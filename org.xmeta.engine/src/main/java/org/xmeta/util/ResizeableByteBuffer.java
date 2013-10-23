/*
    X-Meta Engine。
    Copyright (C) 2013  zhangyuxiang

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    For alternative license options, contact the copyright holder.

    Emil zhangyuxiang@tom.com
 */
package org.xmeta.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * 因ByteBuffer需实现分配长度，而保存事物到文件时是无法确定长度的，所以使用ResizeableByteBuffer
 * 代理ByteBuffer，这里默认分配ByteBuffer 20*1024字节，当越界时在分配一个ByteBuffer，而所有的
 * Bytebuffer放置在一个数组中。<p/>
 * 
 * 此对象是线程不安全的，设计时只是考虑在一个线程里使用。
 * 
 * @author zyx
 *
 */
public class ResizeableByteBuffer {
	ByteBuffer[] buffers = new ByteBuffer[1];

	int index = 0;

	int DEFAULT_SIZE = 20480;

	public ResizeableByteBuffer() {
		buffers = new ByteBuffer[1];
		buffers[index] = ByteBuffer.allocate(DEFAULT_SIZE);
	}
	
	/**
	 * 获取所有的字节缓存。
	 * 
	 * @return
	 */
	public ByteBuffer[] getByteBuffers(){
		return buffers;
	}

	public Buffer flip(){
		return  buffers[index].flip();
	}
	
	private void checkAllocate() {
		if(!buffers[index].hasRemaining()){
			buffers[index].flip();
		}else{
			return;
		}
		
		//重新分配一个数组
		ByteBuffer[] tmpBuffers = new ByteBuffer[buffers.length + 1];
		for (int i = 0; i < buffers.length; i++) {
			tmpBuffers[i] = buffers[i];
		}

		//原有buffer定位
		tmpBuffers[index].flip();

		//分配新的Buffer
		tmpBuffers[index + 1] = ByteBuffer.allocate(DEFAULT_SIZE);

		//索引增加
		index++;
	}

	public ByteBuffer put(byte b) {
		checkAllocate();
		
		return buffers[index].put(b);
	}

	public ByteBuffer put(byte[] src) {
		checkAllocate();
		
		return buffers[index].put(src);
	}

	/**
	 * 相对批量 put 方法（可选操作）。
	 */
	ByteBuffer put(byte[] src, int offset, int length) {
		checkAllocate();
		
		return buffers[index].put(src, offset, length);
	}

	/**
	 * 相对批量 put 方法（可选操作）。
	 */
	public ByteBuffer put(ByteBuffer src) {
		checkAllocate();
		
		return buffers[index].put(src);
	}

	/**
	 * 相对批量 put 方法（可选操作）。
	 */
	public ByteBuffer put(int index_, byte b) {
		checkAllocate();
		
		return buffers[index].put(index_, b);
	}

	public ByteBuffer putChar(char value) {
		checkAllocate();
		
		return buffers[index].putChar(value);
	}

	public ByteBuffer putChar(int index, char value) {
		checkAllocate();
		
		return buffers[index].putChar(index, value);
	}

	public ByteBuffer putDouble(double value) {
		checkAllocate();
		
		return buffers[index].putDouble(value);
	}

	public ByteBuffer putDouble(int index, double value) {
		checkAllocate();
		
		return buffers[index].putDouble(index, value);
	}

	public ByteBuffer putFloat(float value) {
		checkAllocate();
		
		return buffers[index].putFloat(index);
	}

	public ByteBuffer putFloat(int index, float value) {
		checkAllocate();
		
		return buffers[index].putFloat(index, value);
	}

	public ByteBuffer putInt(int value) {
		checkAllocate();
		
		return buffers[index].putInt(index);
	}

	public ByteBuffer putInt(int index, int value) {
		checkAllocate();
		
		return buffers[index].putInt(index, value);
	}

	public ByteBuffer putLong(int index, long value) {
		checkAllocate();
		
		return buffers[index].putLong(index, value);
	}

	public ByteBuffer putLong(long value) {
		checkAllocate();
		
		return buffers[index].putLong(value);
	}

	public ByteBuffer putShort(int index, short value) {
		checkAllocate();
		
		return buffers[index].putShort(index, value);
	}

	public ByteBuffer putShort(short value) {
		checkAllocate();
		
		return buffers[index].putShort(value);
	}

}