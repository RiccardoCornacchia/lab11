package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {

    private static final long WAITING_TIME = TimeUnit.SECONDS.toMillis(10);
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();

    public AnotherConcurrentGUI(){
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        final JButton up = new JButton("up");
        final JButton down = new JButton("down");
        final JButton stop = new JButton("stop");
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        
        final Agent agent = new Agent();
        up.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                agent.countUp();
            }
            
        });

        down.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                agent.countDown();
            }
            
        });
        
        stop.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                agent.stopCounting();
                stop.setEnabled(false);
                up.setEnabled(false);
                down.setEnabled(false);
            }
            
        });
        new Thread(agent).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                agent.stopCounting();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        stop.setEnabled(false);
                        up.setEnabled(false);
                        down.setEnabled(false);
                    }
                });
            }
        }).start();
    }

    private class Agent implements Runnable {
        private volatile boolean stop;
        private volatile boolean up = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    this.counter += up ? 1 : -1;
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeLater(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void countUp(){
            this.up = true;
        }

        public void countDown(){
            this.up = false;
        }
    }
}
