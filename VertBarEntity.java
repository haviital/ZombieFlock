import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class VertBarEntity 
{
    VertBarEntity(float x, float y ) 
    { 
        image = new BarV();
        //velX = 0;
        //velY = 0;
        setX(x);
        setY(y);
        halfWidth = image.width() / 2;
        partCount = 11;
        isHiddenPart = new boolean[partCount];
    }
    
    void setX(float x_) {x = x_; }
    float getX() {return x; }
    void setY(float y_) { y = y_; }
    float getY() { return y; }

    void update()
    {
        long currTime = System.currentTimeMillis();

        // Move the bar.
        if( Button.Left.isPressed() )
            x -= Common.BAR_SPEED;
        else if( Button.Right.isPressed() )
            x += Common.BAR_SPEED;
            
        // Check limits
        if(x<0) x = 0;
        if(x>220-4) x = 220-4;
        
        checkCollisionToZombies();
    }

    void draw(HiRes16Color screen)
    {
        int y2 = 0;
        for(int i = 0; i < partCount; i++  )
        {
            if(!isHiddenPart[i])
                image.draw(screen, x, y + y2);
            y2 += 16;
        }
    }
    
    void checkCollisionToZombies()
    {
        //sprite.draw(screen); // Animation is updated automatically
        
        // Center point of the bar
        float barCenterX = x + halfWidth;
        
        for(int i=0; i < Common.MAX_ZOMBIES; i++)
        {
            ZombieEntity zomb =  Common.zombies[i];
            float zombX = zomb.getX();
            float zombY = zomb.getY();
            float zombW = zomb.sprite.width();
            float zombH = zomb.sprite.height();
            float zombCenterX = zombX + zomb.halfWidth;
            
            float diffOfCenters = barCenterX - zombCenterX;
            if(diffOfCenters>0 && diffOfCenters < (halfWidth + zomb.halfWidth))
            {
                // Check if the bar parts are visible on the zombie
                boolean isHiddenBar = false;
                int partIndexTop = ((int)zombY) >> 4;
                if(partIndexTop >= partCount || partIndexTop < 0 || isHiddenPart[partIndexTop])
                {
                    int partIndexBottom = ((int)(zombY+zombH)) >> 4;
                    if(partIndexBottom >= partCount || partIndexBottom < 0 || isHiddenPart[partIndexBottom])
                       isHiddenBar = true;
                }
                
                // If the bar is visible, put zombie on the left of the bar
                if(!isHiddenBar)
                    Common.zombies[i].setX(x - zombW); 
            }
            else if(diffOfCenters<0 && -diffOfCenters < (halfWidth + zomb.halfWidth))
            {
                // Check if the bar parts are visible on the zombie
                boolean isHiddenBar = false;
                int partIndexTop = ((int)zombY) >> 4;
                if(partIndexTop >= partCount || partIndexTop < 0 || isHiddenPart[partIndexTop])
                {
                    int partIndexBottom = ((int)(zombY+zombH)) >> 4;
                    if(partIndexBottom >= partCount || partIndexBottom < 0 || isHiddenPart[partIndexBottom])
                       isHiddenBar = true;
                }
                
                // If the bar is visible, put zombie on the right of the bar
                if(!isHiddenBar)
                    Common.zombies[i].setX(x + Common.BAR_THICKNESS);
            }
        }
    }
    
    // Member data
    
    public BarV image;
    public float x;
    public float y;
    //public float velX;
    //public float velY;
    public float halfWidth;
    public boolean isHiddenPart[];
    public int partCount;
    
}

