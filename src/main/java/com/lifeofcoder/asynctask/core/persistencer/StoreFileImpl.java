package com.lifeofcoder.asynctask.core.persistencer;

import com.lifeofcoder.asynctask.core.entity.AsyncTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 并发文件存储器，单个文件：暂时不支持并发写入
 *
 * @author xbc
 * @date 2020/1/22
 */
public class StoreFileImpl<T> implements StoreFile<T>, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreFileImpl.class);
    private static final byte[] MAGIC_BYTES = new byte[] {(byte) 0x57, (byte) 0x8A};
    private static final int MAGIC_BYTES_SIZE = MAGIC_BYTES.length;
    private static final int LENGTH_FIELD_SIZE = 4;

    private Long filePosition;
    private int headerSize;
    private int maxDataSize;

    //mmap
    private MappedByteBuffer pageBuffer;

    private Serializer<T> serializer;

    private File file;

    private boolean isLastFile;

    private volatile int wrotePos;

    private volatile int flushedPos;

    private SyncMode syncMode = SyncMode.SYNC;

    /**
     * 异步刷盘周期：milliseconds
     */
    private Long flushPeriod;

    private Thread asyncThread;

    public StoreFileImpl(File folder, Long filePosition, int headerSize, int maxDataSize, boolean isLastFile) {
        this.filePosition = filePosition;
        this.headerSize = headerSize;
        this.maxDataSize = maxDataSize;
        this.isLastFile = isLastFile;
        makeSureFolderExists(folder);
        file = new File(folder, String.valueOf(filePosition));
        if (file.exists()) {
            wrotePos = (int) file.length();
        }

        flushedPos = wrotePos;
        initOrRecoverFile();

        //启动刷盘线程
        if (syncMode == SyncMode.ASYNC) {
            startAsyncThread();
        }
    }

    private void startAsyncThread() {
        asyncThread = new Thread() {
            @Override
            public void run() {
                try {
                    TimeUnit.MILLISECONDS.sleep(flushPeriod);
                }
                catch (InterruptedException e) {
                    interrupt();
                }
                flush();
            }
        };

        asyncThread.start();
    }

    public SyncMode getSyncMode() {
        return syncMode;
    }

    public void asyncFlush(Long flushPeriod) {
        if (flushPeriod < 0) {
            throw new IllegalArgumentException("Flush persion must be larger than 0");
        }

        this.syncMode = SyncMode.ASYNC;
        this.flushPeriod = flushPeriod;
    }

    /**
     * 初始化的时候数据全部变成写入状态:POISTION=数据最大位置， LIMIT=CAP，需要读取自己FLIP
     * 读取方式：
     *  ByteByffer readByffer = pageBuffer.asReadOnlyBuffer();
     *  readByffer.flip();
     *  readByffer.getInt()/get(byte[]) //执行读取操作
     */
    private void initOrRecoverFile() {
        //init
        FileChannel fileChannel = null;
        try {
            int tmpWrotePos = wrotePos;
            fileChannel = openFileChannel();
            if (isLastFile) {
                pageBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, maxDataSize);
            }
            else {
                pageBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, maxDataSize);
            }
            pageBuffer.limit(maxDataSize);
            pageBuffer.position(tmpWrotePos);
        }
        catch (IOException e) {
            throw new AsyncTaskException("Failed to open file channel for file[" + file.getPath() + "].", e);
        }
        finally {
            closeQuietly(fileChannel);
        }

        //recover
        if (file.exists() && isLastFile) {
            recover();
        }
    }

    /**
     * 恢复可能被损坏的文件
     * 最后一个，容易写入的时候可能会出错。
     */
    private void recover() {
        //没有魔数，直接清空文件
        if (wrotePos < MAGIC_BYTES_SIZE) {
            LOGGER.error("T format of file is invalid. The size of file less than magic bytes size.");
            pageBuffer.position(0);
            pageBuffer.limit(0);
            return;
        }

        final byte[] magicBytes = new byte[MAGIC_BYTES_SIZE];
        int dataSize = 0;
        int position = 0;

        //从前到后读取数据，直接找到最后一个不符合条件的
        ByteBuffer readOnlyBuffer = pageBuffer.asReadOnlyBuffer();
        readOnlyBuffer.position(position);
        while (readOnlyBuffer.hasRemaining()) {
            position = readOnlyBuffer.position();
            byte[] bytes = doRead(position);
            if (null == bytes) {
                LOGGER.error("The format of file is in valid. Abandon the error data at the end of the file.");
                break;
            }
            else {
                position += MAGIC_BYTES_SIZE + LENGTH_FIELD_SIZE + bytes.length;
            }
        }
        pageBuffer.position(position);
    }

    private FileChannel openFileChannel() throws IOException {
        if (isLastFile) {
            return FileChannel.open(Paths.get(file.getPath()), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        }
        else {
            return FileChannel.open(Paths.get(file.getPath()), StandardOpenOption.READ);
        }
    }

    private void makeSureFolderExists(File folder) {
        Objects.requireNonNull(folder, "Directory of log file can't be null.");
        if (!folder.isDirectory()) {
            throw new AsyncTaskException(folder.getPath() + " is not directory.");
        }

        if (folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public int append(T data) {
        byte[] dataBytes = serializer.encode(data);
        if (reachFileEndBy(dataBytes.length)) {
            //never happend 上游需要自己判断是否超过文件长度，如果超过，则新建使用新文件追加
            throw new AsyncTaskException("The data can't be appened into the file because of its too big length for the file.");
        }
        pageBuffer.put(dataBytes);

        if (syncMode == SyncMode.SYNC) {
            flush();
        }
        return dataBytes.length;
    }

    private int flush() {
        if (flushedPos < wrotePos) {
            pageBuffer.force();
            flushedPos = wrotePos;
        }
        return flushedPos;
    }

    @Override
    public T read(int position) {
        byte[] bytes = doRead(position);
        if (null == bytes) {
            return null;
        }

        return serializer.decode(bytes);
    }

    private byte[] doRead(int position) {
        if (position > wrotePos) {
            LOGGER.error("The position to read is lager than wrote position.");
            return null;
        }

        final byte[] magicBytes = new byte[MAGIC_BYTES_SIZE];
        int dataSize = 0;
        ByteBuffer readOnlyBuffer = pageBuffer.asReadOnlyBuffer();
        readOnlyBuffer.position(position);
        readOnlyBuffer.get(magicBytes);

        //读取并校验魔数
        //不是魔数，数据异常，放弃后续数据
        if (!Arrays.equals(magicBytes, MAGIC_BYTES)) {
            LOGGER.error("The format of file is invalid. There is no valid magic bytes.");
            return null;
        }

        //读取长度:长度不够
        if (readOnlyBuffer.remaining() < LENGTH_FIELD_SIZE) {
            LOGGER.error("The format of file is invalid. There is no data length field..");
            return null;
        }
        dataSize = readOnlyBuffer.getInt();

        //校验数据:数据长度不够，数据异常
        if (readOnlyBuffer.remaining() < dataSize) {
            LOGGER.error("The format of file is invalid. The data is not completed.");
            return null;
        }
        byte[] dataBytes = new byte[dataSize];
        readOnlyBuffer.get(dataBytes);
        return dataBytes;
    }

    @Override
    public boolean reachFileEndBy(int waitToWroteSize) {
        int tmpWrotePos = wrotePos;
        return tmpWrotePos + waitToWroteSize >= maxDataSize;
    }

    @Override
    public void close() throws IOException {
        flush();
        //回收
        invoke(invoke(pageBuffer, "cleaner"), "clean");
    }

    private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    Method method = method(target, methodName, args);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    private static Method method(Object target, String methodName, Class<?>[] args)
            throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    private void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        }
        catch (IOException e) {
            //ignore
        }
    }

    /**
     * 耍盘模式
     */
    public enum SyncMode {
        SYNC, ASYNC;
    }
}
