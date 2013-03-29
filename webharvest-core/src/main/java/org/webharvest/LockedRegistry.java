/*
 Copyright (c) 2006-2012 the original author or authors.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
*/

package org.webharvest;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Generic locking registry implementation that follows 'decorator' design
 * pattern. Read and write access to {@link org.webharvest.Registry} methods
 * are locked based on {@link java.util.concurrent.locks.ReentrantReadWriteLock}
 * object.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 *
 * @param <K>
 *            type of key under which values are bound
 * @param <V>
 *            type of bound values
 */
public final class LockedRegistry<K, V> implements Registry<K, V> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Registry<K, V> delegate;

    /**
     * Class constructor expecting {@link org.webharvest.Registry} to be
     * specified.
     *
     * @param delegate none locking {@link org.webharvest.Registry} delegate.
     */
    // TODO Missing documentation
    public LockedRegistry(final Registry<K, V> delegate) {
        // FIXME rbala Not needed when instantiating via Guice with @Inject annotation
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate is required");
        }
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V lookup(final K name) {
        lock.readLock().lock();
        try {
            return delegate.lookup(name);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(final K name, final V value) throws AlreadyBoundException {
        lock.writeLock().lock();
        try {
            delegate.bind(name, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbind(final K name) {
        lock.writeLock().lock();
        try {
            delegate.unbind(name);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> listBound() {
        lock.readLock().lock();
        try {
            return delegate.listBound();
        } finally {
            lock.readLock().unlock();
        }
    }

}
