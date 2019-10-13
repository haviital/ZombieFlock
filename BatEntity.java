import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class BatEntity extends Bat3 
{
   
    MainDay mainDay;
    public float velX;
    public float velY;
    public float launchAtScreenTopYOn;
    public float halfWidth;
    public float halfHeight;
    boolean isHit;
    boolean isActive;
    
    // Sounds
    lewfres__wild_animal_scream batSfx;

    BatEntity(MainDay mainDayPar) 
    { 
        mainDay = mainDayPar;
        run();
        
        // Sfx
        batSfx = new lewfres__wild_animal_scream(0);
    }
    
    void playSfx()
    {
        batSfx.play();  // Scream!
    }
    
    void update(HiRes16Color screen)
    {
        if(isActive)
        {
            if(Main.screen.cameraY > launchAtScreenTopYOn)
            {
                x += velX;
                y += velY;
                
            }
            
            if( x < mainDay.levelStartX - 16 || x > mainDay.levelStartX + 220 )
                isActive = false;
        }
    }

    void drawMe(HiRes16Color screen)
    {
        if(isActive)
        {
            setMirrored( velX  <0 );    
            setStatic( false );
            draw(screen);
        }
    }
    
}

