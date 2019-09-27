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

    BatEntity() 
    { 
        velX = 0;
        velY = 0;
        halfWidth = width() / 2;
        halfHeight = height() / 2;
        x = 0;
        y = 0;
        run();
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

