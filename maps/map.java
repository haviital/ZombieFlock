package maps;
// Do not edit.
// This file is generated by tilemap.js on build-time.

import femto.TileSet;
import femto.mode.HiRes16Color;
import static System.memory.*;

public class map extends BaseMap {
    public map(){
        super(mapData.map(), new TileSet[]{
            new Tileset()
        });
    }
    
    void drawDayField(HiRes16Color screen){
        draw(screen, 0);
    }
    int readDayField(int tileX, int tileY){
        pointer addr = this.data + 4 + tileY*28 + tileX*2;
        return (((int)LDRB(addr)&1)<<8) + LDRB(addr+1);
    }

}
    