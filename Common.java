import ZombieEntity;

class Common 
{
    static final String VERSION_STRING = "0.4";

    static final int TARGET_X = 110;
    static final int TARGET_Y = 88;
    static final int ZOMBIES_PER_NEST = 10;
    static final int MAX_ZOMBIES = ZOMBIES_PER_NEST*4;
    static final int MAX_ENEMIES = 3;
    static final int MAX_EVENTS = 10;
    static final int BAR_SPEED = 2;
    static final int BAR_THICKNESS = 4;
    static final int CASTLE_X = 110-28;
    static final int CASTLE_Y = 88-26;
    static final int LEVEL_TIMEOUT = 30 * 1000;  // 30 sec
    static final int BEAN_MAP_TILE_ID = 3; 
    static final int TREE_MAP_TILE_ID = 1;
    static final int SKELETON_OFFSET_Y = -30;
    static final int LEVEL_MAP_COUNT = 6;
    
    static maps.map tilemap;
    static ZombieEntity[] zombies;
    static EnemyEntity[] enemies;
    static HeroEntity heroEntity;    
    static long currentFrameStartTimeMs;
    static Event[] events;
    static long totalBeanCount;
    static long totalCoffeeCount;
    static long currentDay;
    static int[] levelPointsArray;
    
    public static void freeResources()
    {
        tilemap = null;
        zombies = null;
        enemies = null;
        heroEntity = null;
        events = null;
    }
}
