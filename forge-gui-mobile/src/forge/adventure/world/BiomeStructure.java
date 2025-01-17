package forge.adventure.world;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.github.sjcasey21.wavefunctioncollapse.OverlappingModel;
import forge.adventure.data.BiomeStructureData;
import forge.adventure.util.Config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class BiomeStructure {

    private BiomeStructureData data;
    long seed;
    private int biomeWidth;
    private int biomeHeight;
    private int dataMap[][];
    private boolean collisionMap[][];
    boolean init=false;
    private TextureAtlas structureAtlas;
    public BufferedImage image;

    public BiomeStructure(BiomeStructureData data,long seed,int width,int height)
    {
        this.data=data;
        this.seed=seed;
        this.biomeWidth = width;
        this.biomeHeight = height;
    }
    public TextureAtlas atlas() {
        if(structureAtlas==null)
        {
            structureAtlas = Config.instance().getAtlas(data.structureAtlasPath);
        }
        return structureAtlas;
    }
    public int structureObjectCount() {
        return data.mappingInfo.length;
    }

    public int objectID(int x, int y) {

        if(!init)
        {
            initialize();
        }
        if(x>=dataMap.length||x<0||y<0||y>=dataMap[0].length)
            return -1;
        return dataMap[x][y]; 
    }

    public void initialize() {
        init=true;
        OverlappingModel model= new OverlappingModel(sourceImage(),data.N, (int) (data.width* biomeWidth), (int) (data.height*biomeHeight),data.periodicInput,data.periodicOutput,data.symmetry,data.ground);
        HashMap<Integer,Integer> colorIdMap=new HashMap<>();
        for(int i=0;i<data.mappingInfo.length;i++)
        {
                colorIdMap.put(Integer.parseInt(data.mappingInfo[i].color,16),i);
        }
        boolean suc=false;
        for(int i=0;i<10&&!suc;i++)
            suc=model.run((int) seed+(i*5355),15000);
        if(!suc)
        {
            dataMap=new int[(int) (data.width* biomeWidth)][ (int) (data.height*biomeHeight)];
            collisionMap=new boolean[(int) (data.width* biomeWidth)][ (int) (data.height*biomeHeight)];
            return;
        }
        image=model.graphics();
        dataMap=new int[image.getWidth()][image.getHeight()];
        collisionMap=new boolean[image.getWidth()][image.getHeight()];
        for(int x=0;x<image.getWidth();x++)
        {

            for(int y=0;y<image.getHeight();y++)
            {
                int rgb=image.getRGB(x,y) & 0xffffff;
                if(!colorIdMap.containsKey(rgb))
                {
                    dataMap[x][y]=-1;
                }
                else {
                    dataMap[x][y]=colorIdMap.get(rgb);
                    collisionMap[x][y]=data.mappingInfo[colorIdMap.get(rgb)].collision;
                }
            }
        }

    }

    private BufferedImage sourceImage() {
        try {
            return ImageIO.read(new File(Config.instance().getFilePath(data.sourcePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public int x() {
        return (int) ((data.x*biomeWidth)-(data.width*biomeWidth)/2);
    }
    public int y() {
        return (int) ((data.y*biomeHeight)-(data.height*biomeHeight)/2);
    }

    public BiomeStructureData.BiomeStructureDataMapping[] mapping() {
        return data.mappingInfo;
    }


    public boolean collision(int x, int y) {
        if(!init)
        {
            initialize();
        }
        if(x>=collisionMap.length||x<0||y<0||y>=collisionMap[0].length)
            return false;
        return collisionMap[x][y];
    }
}
