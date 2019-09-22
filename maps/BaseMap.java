package maps;
import static System.memory.*;
import femto.mode.HiRes16Color;
import femto.TileSet;

public class BaseMap {
    pointer data;
    TileSet[] sets;
    
    protected public BaseMap( pointer data, TileSet[] sets ){
        this.data = data;
        this.sets = sets;
    }

    void draw( HiRes16Color screen ){
        pointer data = this.data;
        int layerCount = LDRB(data++);
        for( int i=0; i<layerCount; ++i ){
            draw(screen, i);
        }
    }
    
    int width(){
        pointer data = this.data;
        int width = LDRB(data+1);
        return width;
    }
    
    int height(){
        pointer data = this.data;
        int height = LDRB(data+2);
        return height;
    }
    
    int tileWidth(){
        int tileWidth = this.sets[0].width();
        return tileWidth;
    }
    
    int tileHeight(){
        int tileHeight = this.sets[0].height();
        return tileHeight;
    }
    
    void draw( HiRes16Color screen, int layer ){
        pointer data = this.data;
        int layerCount = LDRB(data++);
        if( layer >= layerCount ) return;
        
        int width = LDRB(data++);
        int height = LDRB(data++);
        int tileWidth = this.sets[0].width();
        int tileHeight = this.sets[0].height();
        
        data += layer*(1 + width*2*height);
        int layerFlags = LDRB(data++);
        
        if(layerFlags == 0) // hidden layer
            return;

        for( int y=0; y<height; ++y ){
            for( int x=0; x<width; ++x ){
                byte flags = (byte) LDRB(data++);
                int tile  = (int) LDRB(data++);
                if( (flags&1)==1 ) tile += 0x100;
                if( tile == 0 )
                    continue;
                TileSet tileSet = this.sets[ (flags>>1)&0x07 ];
                tileSet.tileId = tile - 1;
                tileSet.draw(screen, x * tileWidth, y * tileHeight, (flags&0x80)!=0, (flags&0x40)!=0, false);
            }
        }
    }
}