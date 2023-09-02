package sprucegoose.avatarmc.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;

/**
 * Simple Utility to have images as particles
 * @author Backstabber
 *
 */
public class ImageParticles {

    private Map<Vector,Color> particles=new HashMap<Vector, Color>();
    private Vector anchor=new Vector(0,0,0);
    private double ratio=0.2;
    private BufferedImage image;
    private int clearence=300;
    /**
     * Create a new object
     * @param image of the particles structure you want
     * @param scanQuality is the quality of the scanned image in particles (1 for default 2 for half etc)
     */
    public ImageParticles(BufferedImage image,int scanQuality) {
        this.image=image;
        renderParticles(Math.abs(scanQuality));
    }
    /**
     * Set the anchor point for the particles structure
     * by default the anchor will be the bottom right
     * @param x (the x axis of the image or width)
     * @param y (the y axis of the image or height
     */
    public void setAnchor(int x,int y) {
        anchor=new Vector(x, y, 0);
    }
    /**
     * Sets the ratio between blocks & pixels i.e block/pixel (0.1 means 10 pixels in 1 block space)
     * @param ratio
     */
    public void setDisplayRatio(double ratio) {
        this.ratio=ratio;
    }
    /**
     * Get a map of locations & colors on which particles are to be displayed
     * @param location of the anchor point
     * @param pitch (if you want picture to be rotated)
     * @param yaw  (if you want picture to be rotated)
     * @return map of the locations & color
     */
    public Map<Location, Color> getParticles(Location location,double pitch,double yaw) {
        Map<Location, Color> map=new HashMap<Location, Color>();
        for(Vector vector:particles.keySet()) {
            Vector difference=vector.clone().subtract(anchor).multiply(ratio);
            Vector v=rotateAroundAxisX(difference,pitch);
            v=rotateAroundAxisY(v, yaw);
            Location spot=location.clone().add(difference);
            map.put(spot, particles.get(vector));
        }
        return map;
    }
    /**
     * Get a map of locations & colors on which particles are to be displayed
     * @param location
     * @return
     */
    public Map<Location, Color> getParticles(Location location) {
        return getParticles(location, location.getPitch(), location.getYaw());
    }

    private void renderParticles(int sensitivity) {
        int height=image.getHeight();
        int width=image.getWidth();
        for(int x=0;x<width;x=x+sensitivity) {
            for(int y=0;y<height;y=y+sensitivity) {
                int rgb=image.getRGB(x, y);
                if(-rgb<=clearence)
                    continue;
                java.awt.Color javaColor=new java.awt.Color(rgb);
                Vector vector=new Vector((width-1)-x, (height-1)-y, 0);
                particles.put(vector, Color.fromRGB(javaColor.getRed(),javaColor.getGreen(),javaColor.getBlue()));
            }
        }
    }

    private Vector rotateAroundAxisX(Vector v, double angle)
    {
        angle = Math.toRadians(angle);
        double y, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        y = v.getY() * cos - v.getZ() * sin;
        z = v.getY() * sin + v.getZ() * cos;
        return v.setY(y).setZ(z);
    }
    private Vector rotateAroundAxisY(Vector v, double angle)
    {
        angle = -angle;
        angle = Math.toRadians(angle);
        double x, z, cos, sin;
        cos = Math.cos(angle);
        sin = Math.sin(angle);
        x = v.getX() * cos + v.getZ() * sin;
        z = v.getX() * -sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }

    public static void renderImage(Plugin plugin, Player player, String imagePath)
    {
        renderImage(plugin, player.getLocation(), imagePath);
    }

    public static void renderUprightImage(Plugin plugin, Location location, String imagePath)
    {
        location.setPitch(0);
        renderImage(plugin, location, imagePath);
    }

    public static void renderImage(Plugin plugin, Location location, String imagePath)
    {
        File file=new File(plugin.getDataFolder(),imagePath); //location of the image
        BufferedImage image = null;
        try {
            image= ImageIO.read(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        image = resize(image, 50,50);
        ImageParticles particles=new ImageParticles(image,1);

        //width = 50 , height = 10
        particles.setAnchor(25, 25);
        // 0.1 means 10 particles in a block
        particles.setDisplayRatio(0.1);

        //get the image on same orientation as that of the location
        Map<Location, Color> particle = particles.getParticles(location,location.getPitch() ,location.getYaw());

        for(Location spot:particle.keySet()) {
            Color color=particle.get(spot);
            Particle.DustOptions options = new Particle.DustOptions(color, 1);
            //spawn particle at location "spot" with color "color"
            location.getWorld().spawnParticle(Particle.REDSTONE,spot,1,options);
        }
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}

