package com.huinan.server.server.utils;



public abstract class JokerThreadLocal<T> extends ThreadLocal<T> {

    /**
     * Returns the current value of the variable, or {@code null} if it has not
     * been set. This method takes care to remove this {@link ThreadLocal} from
     * the thread if the value has not been set.
     * @return the current value of the variable, or {@code null} if it has not
     * been set.
     */
    @Override
    public final T get()
    {
        T t = super.get();
        if (t == null){
            remove();
        }
        return t;
    }
    
    /**
     * Returns the current value of the variable. If the value has not been
     * set, and {@code create} is {@code true}, then the {@link #create()}
     * method is called, the value of the {@link ThreadLocal} is set to the
     * return value of {@link #create()}, and is returned from this method. If
     * the value has not been set, and {@code create} is {@code false}, then
     * this method behaves exactly the same as the {@link #get()} method.
     * 
     * @return the current value of the variable, or the default value if
     * {@code create} is {@code true}.
     * 
     * @param create whether or not to set the default value if it has not yet
     * been set.
     */
    public T get(boolean create)
    {
        T t = get();
        if (t == null && create){
            t = create();
            set(t);
        }
        return t;
    }
    
    /**
     * Sets the current value. If {@code value} is {@code null}, then this
     * {@link ThreadLocal} is removed from the thread.
     * @param value The value to set.
     */
    @Override
    public void set(T value)
    {
        if (value == null){
            remove();
        }else{
            super.set(value);
        }
    }

    /**
     * Returns the default value for this object.
     * @return the default value for this object.
     * @see #get(boolean) 
     */
    protected abstract T create();
}
