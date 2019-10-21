import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class ZombieClimbEntity extends ZombieClimb 
{
   
    public float velX;
    public float velY;
    boolean isActive;
    long inactivateAtTime;

    ZombieClimbEntity() 
    { 
        velX = 0;
        velY = 0.2;
        y = 0;
        run();
    }
    
    void activate()
    {
        x = 110;
        y = 110;
        isActive = true;
        //inactivateAtTime = Common.currentFrameStartTimeMs + 1500;
    }

    void update(HiRes16Color screen)
    {
        //if(inactivateAtTime!=0 && inactivateAtTime < Common.currentFrameStartTimeMs)
        if( y < 100 )
            isActive = false;
        else
            y -= velY;
    }

    void drawMe(HiRes16Color screen)
    {
        draw(screen);
    }
    
}

