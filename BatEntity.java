import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class BatEntity extends Bat3 
{
   
    public float velX;
    public float velY;
    public float launchAtScreenTopYOn;
    public float halfWidth;
    public float halfHeight;
    boolean isHit;
    boolean isActive;
    
    // Sounds
    lewfres__wild_animal_scream batSfx;

    BatEntity() 
    { 
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
        if(Main.screen.cameraY > launchAtScreenTopYOn)
        {
            x += velX;
            y += velY;
            
        }
    }

    void drawMe(HiRes16Color screen)
    {
        setMirrored( velX  <0 );    
        setStatic( false );
        draw(screen);
    }
    
}

