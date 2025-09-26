import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class ElasticBalls {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String input = JOptionPane.showInputDialog(
                null,
                "Enter number of balls (e.g. 10):", "Number of Balls",
                JOptionPane.QUESTION_MESSAGE
            );
            int n;
            try {
                n = Integer.parseInt(input.trim());
                if (n < 1)
                    n = 10;
            } catch (NumberFormatException e) {
                n = 10;
            }
            JFrame frame = new JFrame("Elastic Balls");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            SimulationPanel panel = new SimulationPanel(n);
            frame.setContentPane(panel);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.startAnimation();
        });
    }
}

class SimulationPanel extends JPanel {
    private final java.util.List<Ball> balls = Collections.synchronizedList(new ArrayList<>());
    private final Random rand = new Random();
    private final int fps = 60;
    private javax.swing.Timer timer;

    SimulationPanel(int nBalls) {
        setBackground(java.awt.Color.BLACK);
        setDoubleBuffered(true);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                initBalls(nBalls);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                if (balls.isEmpty())
                    initBalls(nBalls);
            }
        });
        MouseAdapter ma = new MouseAdapter() {
            Ball picked = null;
            double px, py, pt;

            @Override
            public void mousePressed(MouseEvent e) {
                synchronized (balls) {
                    double mx = e.getX(), my = e.getY();
                    for (Ball b : balls) {
                        double dx = mx - b.x, dy = my - b.y;
                        if (dx * dx + dy * dy <= b.radius * b.radius) {
                            picked = b;
                            px = mx;
                            py = my;
                            pt = System.nanoTime() / 1e9;
                            break;
                        }
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (picked != null) {
                    picked.x = e.getX();
                    picked.y = e.getY();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (picked != null) {
                    double now = System.nanoTime() / 1e9;
                    double dt = Math.max(1e-6, now - pt);
                    double vx = (e.getX() - px) / dt * 0.02;
                    double vy = (e.getY() - py) / dt * 0.02;
                    picked.vx = vx;
                    picked.vy = vy;
                    picked = null;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    double radius = 8 + rand.nextDouble() * 18;
                    java.awt.Color color = new java.awt.Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
                    double mass = Math.PI * radius * radius;
                    balls.add(new Ball(e.getX(), e.getY(), (rand.nextDouble() - 0.5) * 4, (rand.nextDouble() - 0.5) * 4,
                            radius, mass, color));
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    void startAnimation() {
        if (timer != null)
            timer.stop();
        timer = new javax.swing.Timer(1000 / fps, _ -> {
            step();
            repaint();
        });
        timer.start();
    }

    private void initBalls(int n) {
        synchronized (balls) {
            if (!balls.isEmpty())
                return;
            int w = Math.max(100, getWidth());
            int h = Math.max(100, getHeight());
            int attemptsLimit = 2000;
            for (int i = 0; i < n; i++) {
                int attempts = 0;
                Ball b;
                do {
                    double radius = 8 + rand.nextDouble() * 18;
                    double x = radius + rand.nextDouble() * (w - 2 * radius);
                    double y = radius + rand.nextDouble() * (h - 2 * radius);
                    double speed = 1.5 + rand.nextDouble() * 3.5;
                    double angle = rand.nextDouble() * 2 * Math.PI;
                    double vx = speed * Math.cos(angle);
                    double vy = speed * Math.sin(angle);
                    java.awt.Color color = new java.awt.Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
                    double mass = Math.PI * radius * radius;
                    b = new Ball(x, y, vx, vy, radius, mass, color);
                    attempts++;
                    if (attempts > attemptsLimit) {
                        break;
                    }
                } while (isOverlapping(b));
                balls.add(b);
            }
        }
    }

    private boolean isOverlapping(Ball newB) {
        for (Ball other : balls) {
            double dx = newB.x - other.x;
            double dy = newB.y - other.y;
            double dist2 = dx * dx + dy * dy;
            double minDist = newB.radius + other.radius;
            if (dist2 < minDist * minDist)
                return true;
        }
        return false;
    }

    private void step() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0)
            return;
        synchronized (balls) {
            for (Ball b : balls) {
                b.x += b.vx;
                b.y += b.vy;
                if (b.x - b.radius < 0) {
                    b.x = b.radius;
                    b.vx = Math.abs(b.vx);
                } else if (b.x + b.radius > w) {
                    b.x = w - b.radius;
                    b.vx = -Math.abs(b.vx);
                }
                if (b.y - b.radius < 0) {
                    b.y = b.radius;
                    b.vy = Math.abs(b.vy);
                } else if (b.y + b.radius > h) {
                    b.y = h - b.radius;
                    b.vy = -Math.abs(b.vy);
                }
            }
            int size = balls.size();
            for (int i = 0; i < size; i++) {
                Ball a = balls.get(i);
                for (int j = i + 1; j < size; j++) {
                    Ball b = balls.get(j);
                    resolveCollision(a, b);
                }
            }
        }
    }

    private void resolveCollision(Ball a, Ball b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dist = Math.hypot(dx, dy);
        double minDist = a.radius + b.radius;
        if (dist == 0) {
            dx = 0.01 * (Math.random() + 0.1);
            dy = 0.01 * (Math.random() + 0.1);
            dist = Math.hypot(dx, dy);
        }
        if (dist < minDist) {
            double nx = dx / dist;
            double ny = dy / dist;
            double overlap = minDist - dist;
            double percent = 0.8;
            double slop = 0.01;
            double invMassA = 1.0 / a.mass;
            double invMassB = 1.0 / b.mass;
            double correctionMag = Math.max(overlap - slop, 0.0) / (invMassA + invMassB) * percent;
            a.x -= nx * correctionMag * invMassA;
            a.y -= ny * correctionMag * invMassA;
            b.x += nx * correctionMag * invMassB;
            b.y += ny * correctionMag * invMassB;
            double rvx = b.vx - a.vx;
            double rvy = b.vy - a.vy;
            double velAlongNormal = rvx * nx + rvy * ny;
            if (velAlongNormal > 0) {
                return;
            }
            double e = 1.0;
            double j = -(1 + e) * velAlongNormal;
            j /= (invMassA + invMassB);
            double impulseX = j * nx;
            double impulseY = j * ny;
            a.vx -= impulseX * invMassA;
            a.vy -= impulseY * invMassA;
            b.vx += impulseX * invMassB;
            b.vy += impulseY * invMassB;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(java.awt.Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());
        synchronized (balls) {
            for (Ball b : balls) {
                g2.setColor(b.color);
                int drawX = (int) Math.round(b.x - b.radius);
                int drawY = (int) Math.round(b.y - b.radius);
                int d = (int) Math.round(2 * b.radius);
                g2.fillOval(drawX, drawY, d, d);
                g2.setColor(b.color.brighter());
                int hx = (int) Math.round(b.x - b.radius / 3);
                int hy = (int) Math.round(b.y - b.radius / 3);
                int hs = Math.max(1, (int) Math.round(b.radius / 2));
                g2.fillOval(hx, hy, hs, hs);
            }
        }
        g2.dispose();
    }
}

class Ball {
    double x, y;
    double vx, vy;
    double radius;
    double mass;
    java.awt.Color color;

    Ball(double x, double y, double vx, double vy, double radius, double mass, java.awt.Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.mass = mass;
        this.color = color;
    }
}