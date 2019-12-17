package net.epicorp.util;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MemPool<T> implements Supplier<T> {
	private Queue<T> pool = new ArrayDeque<>(128);
	private Supplier<T> supplier;
	private Consumer<T> sanitizer;
	public MemPool(Supplier<T> supplier, Consumer<T> sanitizer) {
		this.supplier = supplier;
	}

	public void ensureCapacity(int size) {
		for (int i = 0; i < Math.max(0, size- this.pool.size()); i++)
			this.expand();
	}

	public void returnObj(T object) {
		this.sanitizer.accept(object);
		this.pool.add(object);
	}

	@Override
	public T get() {
		if(this.pool.isEmpty())
			return this.supplier.get();
		else
			return this.pool.poll();
	}

	private T expand() {
		T object = this.supplier.get();
		this.pool.offer(object);
		return object;
	}

}
