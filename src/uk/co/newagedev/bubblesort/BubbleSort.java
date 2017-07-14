package uk.co.newagedev.bubblesort;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.util.Random;

import javax.swing.JFrame;

public class BubbleSort extends Canvas implements Runnable {

	private static final long serialVersionUID = 6193628460419051013L;
	public static final int WIDTH = 1080, HEIGHT = 720, XOFFSET = 50, YOFFSET = 100;
	public static final Color SELECTED_COLOUR = new Color(0xff0000), SUB_SELECTED_COLOUR = new Color(0xAAAAAA), UNSELECTED_COLOUR = new Color(0xffffff), SOLVED_COLOUR = new Color(0x00ff00);
	public int ups, fps, selectedIndex = -1, completedIndex = -1, numOfItems = 100, comparisonsPerSecond = 60, oldNumOfItems = numOfItems;
	public Slider amountOfValues = new Slider("Amount Of Values: ", 300, 640, 200, 50, 10, 1000, 100);
	public Slider speedOfSort = new Slider("Comparisons per second: ", 550, 640, 480, 50, 10, 100000, 60);
	public Button startSort = new Button("Start Sort", 50, 30, 200, 50, new Runnable() {
		public void run() {
			if (!sorting) {
				if (selectedIndex == -1) {
					selectedIndex = 1;
					completedIndex = 1;
					genNums();
				}
				sorting = true;
			}
		}
	});
	public Button stopSort = new Button("Stop Sort", 300, 30, 200, 50, new Runnable() {
		public void run() {
			sorting = false;
		}
	});
	public int[] nums;
	public JFrame frame;
	public Thread thread;
	public Random rand = new Random();
	public boolean running = true, sorting = false;

	public BubbleSort() {
		frame = new JFrame();
		frame.setTitle("Bubble Sort");
		frame.setSize(WIDTH, (int) (HEIGHT * 1.03));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.pack();
		addMouseListener(amountOfValues);
		addMouseMotionListener(amountOfValues);
		addMouseListener(startSort);
		addMouseMotionListener(startSort);
		addMouseListener(speedOfSort);
		addMouseMotionListener(speedOfSort);
	}

	public synchronized void start() {
		thread = new Thread(this, "Bubble Sort");
		running = true;
		run();
	}

	public synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		long lastTime = System.nanoTime();
		long secondTime = System.currentTimeMillis();
		double ns = 1000000000 / comparisonsPerSecond;
		double delta = 0;
		while (running) {
			render();
			fps++;
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				update();
				ups++;
				delta--;
			}
			if (ns != 1000000000 / comparisonsPerSecond) {
				ns = 1000000000 / comparisonsPerSecond;
			}
			if (System.currentTimeMillis() - secondTime >= 1000) {
				frame.setTitle("Bubble Sort   Number Of Items: " + numOfItems + "   FPS: " + fps + " UPS: " + ups);
				secondTime += 1000;
				fps = 0;
				ups = 0;
			}
		}
	}

	public void bubbleSort() {
		if (nums[selectedIndex - 1] > nums[selectedIndex]) {
			int temp = nums[selectedIndex - 1];
			nums[selectedIndex - 1] = nums[selectedIndex];
			nums[selectedIndex] = temp;
		}
	}

	public void update() {
		comparisonsPerSecond = (int) speedOfSort.getValue();
		if (sorting) {
			bubbleSort();
			if (selectedIndex < numOfItems - completedIndex) {
				selectedIndex++;
			} else {
				selectedIndex = 1;
				completedIndex += 1;
				if (completedIndex >= numOfItems - 1) {
					completedIndex = -1;
					selectedIndex = -1;
					sorting = false;
				}
			}
		} else {
			numOfItems = (int) amountOfValues.getValue();
			if (numOfItems != oldNumOfItems) {
				genNums();
				oldNumOfItems = numOfItems;
			}
		}
	}

	public void genNums() {
		nums = new int[numOfItems];
		for (int i = 0; i < numOfItems; i++) {
			nums[i] = rand.nextInt(4990) + 10;
		}
	}

	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(2);
			return;
		}

		Graphics g = bs.getDrawGraphics();
		g.setColor(new Color(0x555555));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		for (int i = 0; i < numOfItems; i++) {
			g.setColor(i > numOfItems - completedIndex ? SOLVED_COLOUR : (selectedIndex == i ? SUB_SELECTED_COLOUR : (selectedIndex - 1 == i ? SELECTED_COLOUR : UNSELECTED_COLOUR)));
			float width = ((float) (WIDTH - (XOFFSET * 2))) / numOfItems;
			int height = (int) ((HEIGHT - (YOFFSET * 2)) * (nums[i] / maxVal(nums)));
			int x = (int) (XOFFSET + (width * i));
			int y = HEIGHT - YOFFSET - height;
			g.fillRect(x, y, (int) width + 1, height);
		}
		amountOfValues.render(g);
		startSort.render(g);
		speedOfSort.render(g);
		g.dispose();
		bs.show();
	}

	public float maxVal(int[] nums) {
		float maxVal = 0;
		for (int num : nums) {
			maxVal = Math.max(num, maxVal);
		}
		return maxVal;
	}

	public static void main(String[] args) {
		BubbleSort sort = new BubbleSort();
		sort.genNums();
		sort.start();
	}

	public class Slider implements MouseListener, MouseMotionListener {
		public int x, y, width, height, sliderX, oldmx, mx;
		public float valDiff, sliderVal, minVal, maxVal;
		public boolean grabbed = false, hover = false;
		public String label;

		public Slider(String label, int x, int y, int width, int height, int minVal, int maxVal, int startVal) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.minVal = minVal;
			this.maxVal = maxVal;
			this.sliderVal = startVal;
			this.label = label;
			this.valDiff = Math.max(this.minVal, this.maxVal) - Math.min(this.minVal, this.maxVal);
			this.sliderX = (int) (((this.width - 20) / this.valDiff) * this.sliderVal);
		}

		public void render(Graphics g) {
			g.setColor(Color.DARK_GRAY);
			g.fillRect(x - 10, y - 10, width + 20, height + 20);
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(x, y, width, height);
			g.setColor(Color.DARK_GRAY);
			g.fillRect(x + sliderX, y, 20, height);
			if (hover) {
				g.setColor(Color.LIGHT_GRAY);
			} else {
				g.setColor(Color.GRAY);
			}
			g.fillRect(x + sliderX + 5, y + 5, 10, height - 10);
			g.setColor(Color.BLACK);
			g.setFont(new Font("Tahoma", 0, 10));
			g.drawString(label + sliderVal, x + 10, y + (height / 2) + 5);
		}

		public void mouseClicked(MouseEvent me) {
		}

		public void mouseEntered(MouseEvent me) {
		}

		public void mouseExited(MouseEvent me) {
		}

		public void mousePressed(MouseEvent me) {
			if (me.getButton() == MouseEvent.BUTTON1) {
				if (me.getX() > x + sliderX && me.getX() < x + sliderX + 20) {
					if (me.getY() > y && me.getY() < y + height) {
						grabbed = true;
					}
				}
			}
		}

		public void mouseReleased(MouseEvent me) {
			if (me.getButton() == MouseEvent.BUTTON1) {
				if (grabbed) {
					grabbed = false;
				}
			}
			mx = -1;
			oldmx = -1;
		}

		public float getValue() {
			return sliderVal;
		}

		public void mouseDragged(MouseEvent me) {
			if (grabbed) {
				sliderX = me.getX() - x - 10;
				if (sliderX < 0) {
					sliderX = 0;
				}
				if (sliderX > width - 20) {
					sliderX = width - 20;
				}
				sliderVal = minVal + ((valDiff / (width - 20)) * sliderX);
			}
		}

		public void mouseMoved(MouseEvent me) {
			if (me.getX() > x + sliderX && me.getX() < x + sliderX + 20) {
				if (me.getY() > y && me.getY() < y + height) {
					hover = true;
					return;
				}
			}
			hover = false;
		}
	}

	public class Button implements MouseListener, MouseMotionListener {
		public int x, y, width, height;
		public boolean hover = false;
		public String label;
		public Runnable task;

		public Button(String label, int x, int y, int width, int height, Runnable task) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.label = label;
			this.task = task;
		}

		public void render(Graphics g) {
			g.setColor(Color.DARK_GRAY);
			g.fillRect(x - 10, y - 10, width + 20, height + 20);
			if (hover) {
				g.setColor(Color.LIGHT_GRAY);
			} else {
				g.setColor(Color.GRAY);
			}
			g.fillRect(x, y, width, height);
			g.setColor(Color.BLACK);
			g.setFont(new Font("Tahoma", 0, 15));
			g.drawString(label, x + 10, y + (height / 2) + 5);
		}

		public void mouseClicked(MouseEvent me) {
			if (hover) {
				task.run();
			}
		}

		public void mouseEntered(MouseEvent me) {
		}

		public void mouseExited(MouseEvent me) {
		}

		public void mousePressed(MouseEvent me) {
		}

		public void mouseReleased(MouseEvent me) {
		}

		public void mouseDragged(MouseEvent me) {
		}

		public void mouseMoved(MouseEvent me) {
			if (me.getX() > x && me.getX() < x + width) {
				if (me.getY() > y && me.getY() < y + height) {
					hover = true;
					return;
				}
			}
			hover = false;
		}
	}
}
