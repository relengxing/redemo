package com.relengxing.redemo;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ByteUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chaoli
 * @date 2023-10-27 21:31
 * @Description
 **/
@Slf4j
public class MmapContainer {


    /**
     * 映射的文件
     */
    private String mappedFileStr;
    private File mappedFile;

    /**
     * 总字节数
     */
    private Long totalByte;

    /**
     * 总页数
     */
    private Long totalPage;
    /**
     * 每页字节数
     */
    private Integer pageSize = 4 * 1024;       // 4k

    /**
     * Mapping 列表
     * 默认每 1gb 增加一个
     */
    private List<MappedByteBuffer> mappedByteBufferList = new ArrayList<>();


    private Long sizePerFile = 1 * 1024 * 1024 * 1024L;      // 1Gb
    private Long pagePerFile = sizePerFile / pageSize;      // 1Gb/pageSize

    private final ReentrantLock lock = new ReentrantLock(false);

    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final byte[] LINE_SEPARATOR_BYTE = LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8);


    public MmapContainer(String mappedFileStr, Long totalPage) {
        this.mappedFileStr = mappedFileStr;
        this.mappedFile = new File(mappedFileStr);
        this.totalPage = totalPage;
        this.totalByte = totalPage * pageSize;
    }

    @SneakyThrows
    public void init() {
        int mappedCount = (int) Math.ceil((double) totalPage / pagePerFile);
        log.info("init: mappedCount: " + mappedCount);
        for (int i = 0; i < mappedCount; i++) {
            FileChannel fileChannel = FileChannel.open(Paths.get(mappedFile.getPath()),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.READ,
//                StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, sizePerFile * i, sizePerFile);
            mappedByteBufferList.add(mappedByteBuffer);
        }
        log.info("init 结束");
    }

    /**
     * 写一页数据
     *
     * @param page
     * @param data
     * @return
     */
    public void write(Integer page, byte[] data) {
        if (page > totalPage) {
            throw new IndexOutOfBoundsException("超过总页数，写失败");
        }
        if (data.length - 4 > pageSize) {
            throw new IndexOutOfBoundsException("写入内容超过一页容量");
        }
        Integer fileIndex = getFileIndex(page);
        Integer pageIndex = getPageIndex(page);
        ByteBuffer slice = mappedByteBufferList.get(fileIndex).slice();
        slice.position(pageIndex * pageSize);
        int length = data.length;
        slice.put(ByteUtil.intToBytes(length));
        slice.put(data);
        slice.put(LINE_SEPARATOR_BYTE);
    }

    /**
     * 读一页数据
     *
     * @param page
     * @return
     */
    public byte[] read(Integer page) {
        if (page > totalPage) {
            throw new IndexOutOfBoundsException("超过总页数，读失败");
        }
        Integer fileIndex = getFileIndex(page);
        Integer pageIndex = getPageIndex(page);
        ByteBuffer slice = mappedByteBufferList.get(fileIndex).slice();
        slice.position(pageIndex * pageSize);
        byte[] length = new byte[4];
        slice.get(length);
        int dataLength = ByteUtil.bytesToInt(length);
        slice.position(pageIndex * pageSize + 4);
        byte[] data = new byte[dataLength];
        slice.get(data);
        return data;
    }


    /**
     * 获取所在映射的下标
     *
     * @param page
     * @return
     */
    public Integer getFileIndex(Integer page) {
        return Math.toIntExact(page / pagePerFile);
    }

    /**
     * 获取映射对应的行数偏移量
     *
     * @param page
     * @return
     */
    public Integer getPageIndex(Integer page) {
        return Math.toIntExact(page % pagePerFile);
    }


    /**
     * copy from  FileChannelImpl#unmap(私有方法)
     * 解除map
     */
    public void unmap() {
        try {
            lock.lock();
            for (MappedByteBuffer mappedByteBuffer : mappedByteBufferList) {
                if (mappedByteBuffer == null) {
                    return;
                }
                mappedByteBuffer.force();
                Cleaner cl = ((DirectBuffer) mappedByteBuffer).cleaner();
                if (cl != null) {
                    cl.clean();
                }
            }
        } finally {
            lock.unlock();
        }
    }


}
