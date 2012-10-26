package jp.a840.websocket.frame;

/**
 * The Class Maskable.
 *
 * @author Takahiro Hashimoto
 */
public interface Maskable {
    public void mask();
    public void unmask();
}
