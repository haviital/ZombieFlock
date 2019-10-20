import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class BombEntity extends Bomb 
{
    static final long STATE_NOT_VISIBLE = 0;
    static final long STATE_ACTIVATING = 1;
    static final long STATE_ACTIVE = 2;

    static final long DELAY_BEFORE_ACTIVE_MS = 1500;
    static final long DELAY_BEFORE_DISMISS = 5000;
    
    long activateTimeAt;
    long dismissTimeAt;
    long state;
    float halfWidth;
    float halfHeight;
    
    
    // Sounds
    //lewfres__wild_animal_scream batSfx;

    BombEntity() 
    { 
        //run();
        
        // Sfx
        //batSfx = new lewfres__wild_animal_scream(0);
        halfWidth = width() / 2;
        halfHeight = height() / 2;
    }
    
    void playSfx()
    {
        //batSfx.play();  // Scream!
    }
    
    void Launch( float xPar, float yPar )
    {
        state = STATE_ACTIVATING;
        System.out.println("BombEntity.state=STATE_ACTIVATING");
        x = xPar;
        y = yPar;
        activateTimeAt = Common.currentFrameStartTimeMs + DELAY_BEFORE_ACTIVE_MS;  
        dismissTimeAt = activateTimeAt + DELAY_BEFORE_DISMISS;  
        runActivating();
    }

    void update()
    {
        if( activateTimeAt > 0 && activateTimeAt < Common.currentFrameStartTimeMs )
        {
            state = STATE_ACTIVE;
            System.out.println("BombEntity.state=STATE_ACTIVE");
            runActive();
            activateTimeAt = 0;
        }
        if( dismissTimeAt > 0 && dismissTimeAt < Common.currentFrameStartTimeMs )
        {
            state = STATE_NOT_VISIBLE;
            System.out.println("BombEntity.state=STATE_NOT_VISIBLE");
            dismissTimeAt = 0;
        }
    }

    void drawMe(HiRes16Color screen)
    {
        if( state != STATE_NOT_VISIBLE )
        {
            draw(screen);
        }
    }
    
}

