package team.unnamed.mappa.internal.clipboard;

import team.unnamed.mappa.object.Vector;

public interface RealRelative<T> {


    T toRelative(Vector reference, T t);
}
