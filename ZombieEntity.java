import femto.Sprite;
import femto.mode.HiRes16Color;

import Common;

class ZombieEntity 
{
    ZombieEntity( Zombie spritePar ) 
    { 
        sprite = spritePar; 
        speedChangeTime = 0;
        velX = 0;
        velY = 0;
        halfWidth = sprite.width() / 2;
        halfHeight = sprite.height() / 2;
    }
    
    void setX(float x) { sprite.x = x; }
    float getX() {return sprite.x; }
    void setY(float y) { sprite.y = y; }
    float getY() { return sprite.y; }

    void update()
    {
        long currTime = System.currentTimeMillis();
        float previousX = getX();
        float previousY = getY();
        if( currTime > speedChangeTime )
        {
            // Change speed
            int sp = Math.random( 0, 4 );
            float speed = sp * 0.06f + (0.01f * Common.currentDay);
            if( Common.isTutorialActive )
                speed /= 2;  // Slow down in tutorial mode.

            
            // Change direction  
    
            int r = Math.random( 0, 3 );
            // Move horizontally
            if(previousX>Common.TARGET_X && (r==0 || r==2))
                velX =  - speed;
            else if(previousX<Common.TARGET_X && (r==0 || r==2))
                velX =  speed;
            // Move vertically
            if(previousY>Common.TARGET_Y && (r==1 || r==2))
                velY =  - speed;
            else if(previousY<Common.TARGET_Y && (r==1 || r==2))
                velY =  speed;
    
            // Set new change time
            speedChangeTime = currTime + (long)(2*1000);
        }

        // Update pos
        setX( previousX + velX );
        setY( previousY + velY );
    
        // Set mirror if needed.
        sprite.setMirrored( previousX > getX() );
        
    }

    void draw(HiRes16Color screen)
    {
        sprite.draw(screen); // Animation is updated automatically
    }
    
    public Zombie sprite;
    public long speedChangeTime;
    public float velX;
    public float velY;
    public float halfWidth;
    public float halfHeight;
}

