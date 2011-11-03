package des.game.scale;

public class ComponentClass {
    public Class<?> type;
    public int poolSize;
    public ComponentClass(Class<?> classType, int size) {
        type = classType;
        poolSize = size;
    }
}