import femto.Sprite;
import femto.Image;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class HorizBarEntity 
{
   HorizBarEntity(float x, float y ) 
    { 
        image =  new BarH();
        //velX = 0;
        //velY = 0;
        setX(x);
        setY(y);
        halfHeight = image.height() / 2;
        partCount = 14;
        isHiddenPart = new boolean[partCount];
   }
    
    void setX(float x_) {x = x_; }
    float getX() {return x; }
    void setY(float y_) { y = y_; }
    float getY() { return y; }

    void update()
    {
        long currTime = System.currentTimeMillis();

        oldX = x;
        oldY = y;

        // Move the bar.
        if( Button.Up.isPressed() )
            y -= Common.BAR_SPEED;
        else if( Button.Down.isPressed() )
            y += Common.BAR_SPEED;
            
        // Check limits
        if(y<0) y = 0;
        if(y>176-4) y = 176-4;

        checkCollisionToZombies();
    }

    void draw(HiRes16Color screen)
    {
        int x2 = 0;
        for(int i = 0; i < partCount; i++  )
        {
            if(!isHiddenPart[i])
                image.draw(screen, x + x2, y);
            x2 += 16;
        }
    }
    
    void checkCollisionToZombies()
    {
        //sprite.draw(screen); // Animation is updated automatically
        
        // Center point of the bar
        float barCenterY = y + halfHeight;
        
        for(int i=0; i < Common.MAX_ZOMBIES; i++)
        {
            ZombieEntity zomb =  Common.zombies[i];
            float zombX = zomb.getX() + 2;
            float zombY = zomb.getY();
            float zombW = zomb.sprite.width() - 4;
            float zombH = zomb.sprite.height();
            float zombCenterY = zombY + zomb.halfHeight;
            
            float diffOfCenters = barCenterY - zombCenterY;
            if(diffOfCenters>0 && diffOfCenters < (halfHeight + zomb.halfHeight))
            {
                
                // Check if the bar parts are visible on the zombie
                boolean isHiddenBar = false;
                int partIndexLeft = ((int)zombX) >> 4;
                if(partIndexLeft >= partCount || partIndexLeft < 0 || isHiddenPart[partIndexLeft])
                {
                    int partIndexRight = ((int)(zombX+zombW)) >> 4;
                    if(partIndexRight >= partCount || partIndexRight < 0 || isHiddenPart[partIndexRight])
                       isHiddenBar = true;
                }
                
                // If the bar is visible, put zombie above the bar
                if(!isHiddenBar)
                    Common.zombies[i].setY(y - zombH);
                //else
                //    System.out.println("zombX=" + (int)zombX + ", partIndexLeft=" + partIndexLeft + ", partIndexRight=" + partIndexRight);
            }
            else if(diffOfCenters<0 && -diffOfCenters < (halfHeight + zomb.halfHeight))
            {
                // Check if the bar parts are visible on the zombie
                boolean isHiddenBar = false;
                int partIndexLeft = ((int)zombX) >> 4;
                if(partIndexLeft >= partCount || partIndexLeft < 0 || isHiddenPart[partIndexLeft])
                {
                    int partIndexRight = ((int)(zombX+zombW)) >> 4;
                    if(partIndexRight >= partCount || partIndexRight < 0 || isHiddenPart[partIndexRight])
                       isHiddenBar = true;
                }
                
                // If the bar is visible, put zombie below the bar
                if(!isHiddenBar)
                    Common.zombies[i].setY(y + Common.BAR_THICKNESS);
            }
        }
    }
    
    // Member data
    
    public BarH image;
    public boolean test;
    public float x;
    public float y;
    public float oldX;
    public float oldY;
    public float halfHeight;
    public boolean isHiddenPart[];
    public int partCount;
    
}

