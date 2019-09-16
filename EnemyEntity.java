import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class EnemyEntity extends Enemy 
{
   
    EnemyEntity() 
    { 
        velX = 1.5;
        velY = 0;
        halfWidth = width() / 2;
        halfHeight = height() / 2;
        x = 0;
        y = 0;
    }
    
    void update(HiRes16Color screen)
    {
        if( x + followHeroOffsetX - 4 > Common.heroEntity.x )
            x -= velX;
        else if( x + followHeroOffsetX + 4 < Common.heroEntity.x )
            x += velX;
            
        y = Common.heroEntity.y + followHeroOffsetY;

    }

    void drawMe(HiRes16Color screen)
    {
        draw(screen);
    }
    
    public float velX;
    public float velY;
    public float halfWidth;
    public float halfHeight;
    float followHeroOffsetX;
    float followHeroOffsetY;

}

