package it.uniba.gruppo5.tourapp.firebase;

public interface ReadValueListener<T>
{
    void onDataRead(T result);
}
