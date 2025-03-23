import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {



    class  Block {
        int x;
        int y;
        int width;
        int height;
        Image img;
        boolean alive = true;
        boolean used = false;

        Block(int x, int y, int width, int height, Image img){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }


    //board
    int tileSize = 20;
    int rows = 30;
    int columns = rows;
    int boardWidth = tileSize * columns; //32x16=512
    int boardHeight = tileSize * rows;

    Image shipImg;
    Image alienImg;
    Image alienCyanImg;
    Image alienMagentaImg;
    Image alienYellowImg;
    ArrayList<Image> alienImgArray;

    //ship
    int shipWidth = tileSize*2; //64px
    int shipHeight = tileSize;  //32px
    int shipX = tileSize * columns / 2 - tileSize;
    int shipY = boardHeight - tileSize * 2;
    int shipVelocityX = tileSize;

    //aliens
    ArrayList<Block> alienArray;
    int alienWidth = tileSize * 2;
    int alienHeight = tileSize;
    int alienX = tileSize;
    int alienY = tileSize;

    int alienRows = 2;
    int alienColumns = 3;
    int alienCount = 0; //no. of aliens t o defeat
    int alienVelocityX = 1; // alien moving speed;

    //bullets
    ArrayList<Block> bulletArray;
    int bulletWidth = tileSize / 8;
    int bulletHeight = tileSize / 2;
    long bulletVelocityY = -10; //bullet moving speed
    int bulletsToShoot = 1; // default : shoot one bullet

    //Enemy Bullets
    ArrayList<Block> enemyBulletArray;
    int enemyBulletWidth = bulletWidth;
    int enemyBulletHeight = bulletHeight;
    int enemyBulletVelocityY = 1; // speed of enemy bullets
    Random random = new Random();

    //Power-Ups
    ArrayList<Block> powerUpArray = new ArrayList<>();
    int powerUpWidth = tileSize;
    int powerUpHeight = tileSize;
    int powerUpVelocityY = 2;
    boolean powerUpActive = false;
    long powerUpTimer = 0;
//    long powerUpDuration = 5000; //we do not need and like Duration



    Block ship;

    Timer gameLoop;
    int score = 0;
    boolean gameOver = false;


    SpaceInvaders() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        //load images
        shipImg = new ImageIcon(getClass().getResource("./images/ship.png")).getImage();
        alienImg = new ImageIcon(getClass().getResource("./images/alien.png")).getImage();
        alienCyanImg = new ImageIcon(getClass().getResource("./images/alien-cyan.png")).getImage();
        alienMagentaImg = new ImageIcon(getClass().getResource("./images/alien-magenta.png")).getImage();
        alienYellowImg = new ImageIcon(getClass().getResource("./images/alien-yellow.png")).getImage();

        alienImgArray = new ArrayList<Image>();
        alienImgArray.add(alienImg);
        alienImgArray.add(alienCyanImg);
        alienImgArray.add(alienMagentaImg);
        alienImgArray.add(alienYellowImg);

        ship = new Block(shipX, shipY, shipWidth, shipHeight, shipImg);
        alienArray = new ArrayList<Block>();
        bulletArray = new ArrayList<Block>();
        enemyBulletArray = new ArrayList<>();

        //game timer
        gameLoop = new Timer(1000/60, this); //60 fps
//        gameLoop = new Timer(10, this); // faster start
        createAliens();
        gameLoop.start();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        //ship
        g.drawImage(ship.img, ship.x, ship.y, ship.width, ship.height, null);

        //aliens
        for (int i = 0; i < alienArray.size(); i++){
            Block alien = alienArray.get(i);
            if (alien.alive){
                g.drawImage(alien.img, alien.x, alien.y, alien.width, alien.height, null);

            }
        }

        //bullets
        g.setColor(Color.WHITE);
        for (int i =0; i < bulletArray.size(); i++){
            Block bullet = bulletArray.get(i);
            if (!bullet.used) {
//                g.drawRect(bullet.x, bullet.y, bullet.width, bullet.height);
                g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
            }
        }

        //enemy bullets
        g.setColor(Color.RED);
        for (Block enemyBullet : enemyBulletArray){
            g.fillRect(enemyBullet.x, enemyBullet.y, enemyBulletWidth, enemyBulletHeight);
        }

        //Draw power ups
        g.setColor(Color.BLUE);
        for (int i = 0; i < powerUpArray.size(); i++) {
            Block powerUp = powerUpArray.get(i);
            g.fillOval(powerUp.x, powerUp.y, powerUpWidth, powerUpHeight);
        }

        //score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), 10, 35);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Press any key to restart", boardWidth / 2 - 120, boardHeight / 2);
        }
        else {
            g.drawString(String.valueOf(score), 10, 35);
        }

    }

    public void move(){
        //aliens
        for (int i = 0; i < alienArray.size(); i++){
            Block alien = alienArray.get(i);
            if (alien.alive) {
                alien.x += alienVelocityX;

                //if alien touches the borders
                if (alien.x + alien.width >= boardWidth || alien.x <= 0) {
                    alienVelocityX *= -1;
//                    alien.x += alienVelocityX * 2;

                    //move aliens down one row
                    for (int j = 0; j < alienArray.size(); j++){
                        alienArray.get(j).y += alienHeight;
                    }
                }

                //aliens Randomly fire bullets
                if (random.nextInt(100) < 0.1) { // 0.1% chance to fire per frame
                    Block enemyBullet = new Block(alien.x + alien.width / 2, alien.y + alien.height, enemyBulletWidth, enemyBulletHeight, null);
                    enemyBulletArray.add(enemyBullet);
                }

                //enemy touches the ship
                if (alien.y >= ship.y) {
                    gameOver = true;
                }

                //Move enemy bullets
                for (int b = 0; b < enemyBulletArray.size(); b++){
                    Block enemyBullet = enemyBulletArray.get(b);
                    enemyBullet.y += enemyBulletVelocityY;

                    //check if enemy bullet hits the player
                    if (detectCollision(enemyBullet, ship)){
//                        gameOver = true;
                    }
                }

                enemyBulletArray.removeIf(bullet -> bullet.y > boardHeight);
            }
        }

        //player bullets
        for (int i = 0; i < bulletArray.size(); i++){
            Block bullet = bulletArray.get(i);
            bullet.y += bulletVelocityY;

            //bullet collision with aliens
            for (int j = 0; j < alienArray.size(); j++){
                Block alien = alienArray.get(j);
                if (!bullet.used && alien.alive && detectCollision(bullet, alien)){
                    bullet.used = true;
                    alien.alive = false;
                    alienCount --;
                    score += 100;

                    //chance to spawn power-ups
                    if (random.nextInt(100) < 20) { //20% chance to spawn a power up
                        Block powerUp = new Block(alien.x + alien.width / 2, alien.y, powerUpWidth, powerUpHeight, null);
                        powerUpArray.add(powerUp);
                    }
                }
            }
        }

        //clear bullets
        while (bulletArray.size() > 0 && (bulletArray.get(0).used || bulletArray.get(0).y < 0)){
            bulletArray.remove(0);
        }


        //Power Ups
        for (int i = 0; i < powerUpArray.size(); i++){
            Block powerUp = powerUpArray.get(i);
            powerUp.y += powerUpVelocityY;

            //remove power-ups off the screen
            if (powerUp.y > boardHeight){
                powerUpArray.remove(i);
                i--;
            }

            //check if player collects power up
            if (detectCollision(powerUp, ship)){
                activatePowerUp();
                powerUpArray.remove(i);
                i--;
            }
         }



        //next level
        if (alienCount == 0) {
            //increase the number of aliens in columns and rows by 1
            score += alienColumns * alienRows * 100;
            alienColumns = Math.min(alienColumns + 1, columns / 2 - 2);
            alienRows = Math.min(alienRows + 1, rows - 6);
            alienArray.clear();
            bulletArray.clear();
            createAliens();
//            alienVelocityX += 0.1;
        }
    }

    public void activatePowerUp() {
        powerUpActive = true;
//        powerUpStartTime = System.currentTimeMillis();
//        bulletVelocityY *= 1.2;
        bulletsToShoot++;

        // Optionally limit how many bullets the player can shoot
//        if (bulletsToShoot > 5) { // For example, max 5 bullets at once
//            bulletsToShoot = 5;
//        }
    }

    public void createAliens(){
        Random random = new Random();

        //reset aliens to the top left
        alienY = tileSize;
        alienArray.clear();

        for (int r = 0; r < alienRows; r++){
            for (int c = 0; c < alienColumns; c++){
                int randomImgIndex = random.nextInt(alienImgArray.size());
                Block alien = new Block(
                        alienX + c * alienWidth,
                        alienY + r * alienHeight,
                        alienWidth,
                        alienHeight,
                        alienImgArray.get(randomImgIndex)
                );
                alienArray.add(alien);
            }
        }
        alienCount = alienArray.size();
    }

    public boolean detectCollision(Block a, Block b){ //collision formula
        return a.x < b.x + b.width &&               //it just works
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height >  b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver){
            ship.x = shipX;
            alienArray.clear();
            bulletArray.clear();
            enemyBulletArray.clear();
            powerUpArray.clear();
            score = 0;
            alienVelocityX = 1;
            alienColumns = 3;
            alienRows = 2;
            bulletVelocityY = -10;
            powerUpActive = false;
            gameOver = false;
            createAliens();
            gameLoop.start();
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT && ship.x - shipVelocityX >= 0) {
            ship.x -= shipVelocityX;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship.x + ship.width + shipVelocityX <= boardWidth) {
            ship.x += shipVelocityX;
        }
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            //calculate the center of the ship
            int centerX = ship.x + ship.width / 2;

            int offsetX = 0;
            for (int i = 0; i < bulletsToShoot; i++) {
                Block bullet = new Block(
                        centerX + offsetX, // Spread bullets evenly
                        ship.y,
                        bulletWidth,
                        bulletHeight,
                        null
                );
                bulletArray.add(bullet);

                // Update offset for next bullet to the left and right
                offsetX += (i % 20 == 0) ? -bulletWidth : bulletWidth;
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_P) {         //cheat code "P" to PowerUp
            activatePowerUp();
        }
    }


}
