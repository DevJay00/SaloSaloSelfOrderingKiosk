import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.io.File;
import javax.imageio.ImageIO;

public class SaloSaloKiosk extends JFrame {

    // ── Color Palette ──────────────────────────────────────────
    static final Color BG          = new Color(255, 253, 208);
    static final Color ACCENT      = new Color(180,  40,  40);   // deep red
    static final Color GOLD        = new Color(212, 160,  23);
    static final Color DARK        = new Color( 45,  25,  10);
    static final Color CARD_BG     = new Color(255, 250, 225);
    static final Color SECTION_HDR = new Color(120,  20,  20);
    static final Color BTN_GREEN   = new Color( 34, 139,  34);
    static final Color BTN_GREY    = new Color(130, 130, 130);
    static final Color GCASH_BLUE  = new Color( 0,  100, 200);
    static final Color CARD_COLOR  = new Color( 30,  30, 120);

    // ── Menu Data ──────────────────────────────────────────────
    record MenuItem(String name, int price, String imagePath) {}

    static final List<MenuItem> BREAKFAST = List.of(
        new MenuItem("Tapsilog",   130, "images/tapsilog.png"),
        new MenuItem("Tocilog",    110, "images/tocilog.png"),
        new MenuItem("Bangsilog",  140, "images/bangsilog.png"),
        new MenuItem("Longsilog",  105, "images/longsilog.png"),
        new MenuItem("Chicksilog", 120, "images/chicksilog.png")
    );
    static final List<MenuItem> MAINS = List.of(
        new MenuItem("Chicken Adobo",              130, "images/adobo.png"),
        new MenuItem("Sinigang na Baboy",          180, "images/sinigang.png"),
        new MenuItem("Kare-Kare",                  250, "images/karekare.png"),
        new MenuItem("Batchoy",                    150, "images/batchoy.png"),
        new MenuItem("Special Batchoy w/fresh egg",100, "images/batchoy_special.png"),
        new MenuItem("6pcs. Tisa Siomai",           40, "images/siomai.png"),
        new MenuItem("6pcs. Golden Siomai",         50, "images/siomai_golden.png"),
        new MenuItem("6pcs. Japanese Siomai",       60, "images/siomai_japanese.png")
    );
    static final List<MenuItem> GRILLED = List.of(
        new MenuItem("Pork Liempo",  150, "images/liempo.png"),
        new MenuItem("Lechon Kawali",160, "images/lechon.png"),
        new MenuItem("Crispy Pata",  750, "images/pata.png")
    );
    static final List<MenuItem> BEVERAGES = List.of(
        new MenuItem("Kapeng Barako",    30, "images/barako.png"),
        new MenuItem("Leche Flan",       50, "images/lecheflan.png"),
        new MenuItem("Turon",            20, "images/turon.png"),
        new MenuItem("Halo-Halo Espesyal",50,"images/halohalo.png"),
        new MenuItem("Buko Juice",       50, "images/buko.png"),
        new MenuItem("Sago't Gulaman",   30, "images/sagogulaman.png"),
        new MenuItem("Coke",             40, "images/coke.png"),
        new MenuItem("Sprite",           40, "images/sprite.png"),
        new MenuItem("Royal",            40, "images/royal.png"),
        new MenuItem("Coke Zero",        40, "images/cokezero.png"),
        new MenuItem("Iced Tea",         35, "images/icedtea.png")
    );

    // ── Order State ────────────────────────────────────────────
    final Map<String, int[]> order = new LinkedHashMap<>();  // name→[price,qty]
    JPanel orderListPanel;
    JLabel totalLabel;
    JPanel mainContent;
    CardLayout cardLayout;

    // ── Constructor ────────────────────────────────────────────
    public SaloSaloKiosk() {
        setTitle("Salo-Salo Self-Ordering Kiosk");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        getContentPane().setBackground(BG);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(BG);

        mainContent.add(buildWelcomePanel(), "welcome");
        mainContent.add(buildMenuPanel(),    "menu");
        mainContent.add(buildPaymentPanel(), "payment");

        add(mainContent);
        setVisible(true);

        // Auto-switch welcome → menu after 3.5 s with fade
        Timer t = new Timer(3500, e -> fadeToMenu());
        t.setRepeats(false);
        t.start();
    }

    // ══════════════════════════════════════════════════════════
    //  WELCOME SCREEN
    // ══════════════════════════════════════════════════════════
    private JPanel buildWelcomePanel() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // radial gradient background
                int w = getWidth(), h = getHeight();
                g2.setColor(BG);
                g2.fillRect(0,0,w,h);
                RadialGradientPaint rg = new RadialGradientPaint(
                    w/2f, h/2f, Math.max(w,h)*0.7f,
                    new float[]{0f,1f},
                    new Color[]{new Color(255,220,100,120), new Color(255,253,208,0)}
                );
                g2.setPaint(rg);
                g2.fillRect(0,0,w,h);
                // decorative circles
                g2.setColor(new Color(180,40,40,30));
                g2.fillOval(w-300,h-300,400,400);
                g2.fillOval(-100,-100,300,300);
            }
        };
        p.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0; gbc.gridy=GridBagConstraints.RELATIVE;
        gbc.anchor=GridBagConstraints.CENTER; gbc.insets=new Insets(12,0,12,0);

        // Logo plate
        JLabel logo = new JLabel(makeFoodIcon(), SwingConstants.CENTER);
        p.add(logo, gbc);

        // Restaurant name
        JLabel title = new JLabel("<html><center>SALO-SALO</center></html>");
        title.setFont(new Font("Serif", Font.BOLD, 82));
        title.setForeground(ACCENT);
        p.add(title, gbc);

        JLabel tagline = new JLabel("Lutong-Bahay na Pampamilya");
        tagline.setFont(new Font("Serif", Font.ITALIC, 30));
        tagline.setForeground(DARK);
        p.add(tagline, gbc);

        JSeparator sep = new JSeparator();
        sep.setPreferredSize(new Dimension(500,3));
        sep.setForeground(GOLD);
        p.add(sep, gbc);

        JLabel welcome = new JLabel("<html><center>Mabuhay! Tap anywhere to start ordering.</center></html>", SwingConstants.CENTER);
        welcome.setFont(new Font("SansSerif", Font.PLAIN, 26));
        welcome.setForeground(DARK);
        p.add(welcome, gbc);

        // Blinking tap label
        JLabel tap = new JLabel("▶  TAP TO ORDER  ◀");
        tap.setFont(new Font("SansSerif", Font.BOLD, 22));
        tap.setForeground(GOLD);
        p.add(tap, gbc);

        Timer blink = new Timer(700, e -> tap.setVisible(!tap.isVisible()));
        blink.start();

        p.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ fadeToMenu(); blink.stop(); }
        });

        return p;
    }

    private Icon makeFoodIcon() {
        int sz = 160;
        BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // plate
        g.setColor(new Color(255,255,255,220));
        g.fillOval(10,10,140,140);
        g.setColor(GOLD);
        g.setStroke(new BasicStroke(6));
        g.drawOval(10,10,140,140);
        // inner ring
        g.setColor(new Color(212,160,23,80));
        g.fillOval(25,25,110,110);
        // food emoji rendered as string
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        g.drawString("🍽", 28, 108);
        g.dispose();
        return new ImageIcon(img);
    }

    private void fadeToMenu() {
        cardLayout.show(mainContent, "menu");
    }

    // ══════════════════════════════════════════════════════════
    //  MENU PANEL - Main Display with Category Tabs
    // ══════════════════════════════════════════════════════════
    private JPanel buildMenuPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        // ── Top header bar ──
        root.add(buildMenuHeader(), BorderLayout.NORTH);

        // ── Center: Tabbed category panels ──
        JTabbedPane tabs = buildCategoryTabs();

        // ── Right: order summary ──
        JPanel orderPanel = buildOrderPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, orderPanel);
        split.setResizeWeight(0.75);
        split.setDividerSize(4);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        return root;
    }

    private JTabbedPane buildCategoryTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 16));
        tabs.setBackground(BG);
        tabs.setForeground(DARK);

        // Style the tab bar
        UIManager.put("TabbedPane.selected", new Color(255, 240, 150));
        UIManager.put("TabbedPane.contentAreaColor", BG);
        UIManager.put("TabbedPane.tabInsets", new Insets(10, 18, 10, 18));

        String[][] categories = {
            {"🌅 Breakfast",          "BREAKFAST"},
            {"🍲 Main Cuisines",      "MAINS"},
            {"🔥 Grilled & Fried",    "GRILLED"},
            {"🥤 Beverages & Desserts","BEVERAGES"}
        };

        for (String[] cat : categories) {
            List<MenuItem> items = getMenuItemsByCategory(cat[1]);
            tabs.addTab(cat[0], buildCategoryPanel(items));
        }

        return tabs;
    }

    /**
     * Builds a scrollable panel whose items reflow into a responsive grid.
     * Items are laid out 4 columns wide by default, wrapping to fewer columns
     * when the panel is narrower (flex-like behaviour on resize).
     */
    private JScrollPane buildCategoryPanel(List<MenuItem> items) {
        // CARD_W x CARD_H is the preferred card size (must match buildItemCard)
        final int CARD_W = 220;
        final int GAP    = 16;

        // Grid panel – columns computed dynamically
        JPanel grid = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                // Report preferred size so the scroll pane knows the height
                int cols = computeCols();
                int rows = (int) Math.ceil((double) items.size() / cols);
                int w = getParent() != null ? getParent().getWidth() : CARD_W * 4 + GAP * 5;
                int h = rows * (280 + GAP) + GAP;
                return new Dimension(w, h);
            }

            private int computeCols() {
                int available = getParent() != null ? getParent().getWidth() - GAP * 2 : CARD_W * 4 + GAP * 5;
                int cols = Math.max(1, available / (CARD_W + GAP));
                return Math.min(cols, 4); // cap at 4 columns
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        grid.setBackground(BG);
        grid.setOpaque(true);

        // Lay out cards manually when the panel resizes
        grid.setLayout(null); // We'll position children in a ComponentListener

        for (MenuItem item : items) {
            grid.add(buildItemCard(item));
        }

        // Re-layout cards on every resize
        grid.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutCards(grid, items.size(), CARD_W, GAP);
            }
        });

        JScrollPane scroll = new JScrollPane(grid,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);

        // Re-layout when viewport itself resizes
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Sync grid width to viewport
                grid.setSize(scroll.getViewport().getWidth(), grid.getHeight());
                layoutCards(grid, items.size(), CARD_W, GAP);
                grid.revalidate();
                grid.repaint();
            }
        });

        return scroll;
    }

    /** Positions all child cards in a responsive grid (up to 4 columns). */
    private void layoutCards(JPanel grid, int count, int cardW, int gap) {
        int available = grid.getWidth() - gap * 2;
        int cols = Math.max(1, Math.min(4, available / (cardW + gap)));
        int cellW = (available - gap * (cols - 1)) / cols; // stretch cards to fill width
        int cellH = 280;

        int x = gap, y = gap;
        for (int i = 0; i < grid.getComponentCount(); i++) {
            Component c = grid.getComponent(i);
            c.setBounds(x, y, cellW, cellH);
            x += cellW + gap;
            if ((i + 1) % cols == 0) {
                x = gap;
                y += cellH + gap;
            }
        }

        // Update preferred height so scroll pane reacts
        int rows = (int) Math.ceil((double) count / cols);
        int totalH = rows * (cellH + gap) + gap;
        grid.setPreferredSize(new Dimension(grid.getWidth(), totalH));
        grid.revalidate();
        grid.repaint();
    }

    private List<MenuItem> getMenuItemsByCategory(String categoryKey) {
        return switch(categoryKey) {
            case "BREAKFAST" -> BREAKFAST;
            case "MAINS"     -> MAINS;
            case "GRILLED"   -> GRILLED;
            case "BEVERAGES" -> BEVERAGES;
            default          -> List.of();
        };
    }

    private JPanel buildMenuHeader() {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setPaint(new GradientPaint(0,0,ACCENT,getWidth(),0,new Color(120,20,20)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        hdr.setPreferredSize(new Dimension(0,72));
        hdr.setBorder(new EmptyBorder(0,24,0,24));

        JLabel name = new JLabel("🍽  SALO-SALO");
        name.setFont(new Font("Serif", Font.BOLD, 36));
        name.setForeground(Color.WHITE);
        hdr.add(name, BorderLayout.WEST);

        JLabel sub = new JLabel("Lutong-Bahay na Pampamilya  |  Self-Ordering Kiosk");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 16));
        sub.setForeground(new Color(255,220,180));
        hdr.add(sub, BorderLayout.CENTER);

        JButton back = roundButton("◀ Welcome", BTN_GREY, Color.WHITE);
        back.addActionListener(e -> cardLayout.show(mainContent, "welcome"));
        hdr.add(back, BorderLayout.EAST);

        return hdr;
    }

    private JPanel buildItemCard(MenuItem item) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,18,18));
                g2.setColor(new Color(212,160,23,100));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,18,18));
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(220, 280));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Image area
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(196, 130));
        imageLabel.setBackground(new Color(240, 240, 240));
        imageLabel.setOpaque(true);
        
        // Load image or use fallback
        ImageIcon icon = loadItemImage(item.imagePath(), 196, 130);
        if (icon != null) {
            imageLabel.setIcon(icon);
        } else {
            imageLabel.setText("📷 No Image");
            imageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        }
        
        card.add(imageLabel, BorderLayout.NORTH);

        // Name
        JLabel name = new JLabel("<html><center>"+item.name()+"</center></html>", SwingConstants.CENTER);
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        name.setForeground(DARK);
        card.add(name, BorderLayout.CENTER);

        // Price + Add button
        JPanel bottom = new JPanel(new BorderLayout(6, 0));
        bottom.setOpaque(false);
        JLabel price = new JLabel("₱"+item.price(), SwingConstants.LEFT);
        price.setFont(new Font("SansSerif", Font.BOLD, 15));
        price.setForeground(ACCENT);
        bottom.add(price, BorderLayout.WEST);

        JButton add = roundButton("+ Add", ACCENT, Color.WHITE);
        add.setFont(new Font("SansSerif", Font.BOLD, 12));
        add.setPreferredSize(new Dimension(70, 32));
        add.addActionListener(e -> addToOrder(item));
        bottom.add(add, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);

        // Hover effect
        card.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){card.setBorder(new CompoundBorder(new LineBorder(ACCENT,2,true),new EmptyBorder(10,10,10,10)));card.repaint();}
            public void mouseExited(MouseEvent e){card.setBorder(new EmptyBorder(12,12,12,12));card.repaint();}
        });
        return card;
    }

    private ImageIcon loadItemImage(String imagePath, int width, int height) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                BufferedImage img = ImageIO.read(imageFile);
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════
    //  ORDER PANEL
    // ══════════════════════════════════════════════════════════
    private JPanel buildOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0,8));
        panel.setBackground(new Color(245,238,195));
        panel.setBorder(new EmptyBorder(12,12,12,12));
        panel.setPreferredSize(new Dimension(300,0));

        JLabel hdr = new JLabel("🧾  Your Order");
        hdr.setFont(new Font("Serif", Font.BOLD, 22));
        hdr.setForeground(DARK);
        hdr.setBorder(new EmptyBorder(0,0,8,0));
        panel.add(hdr, BorderLayout.NORTH);

        orderListPanel = new JPanel();
        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
        orderListPanel.setBackground(new Color(245,238,195));

        JScrollPane scroll = new JScrollPane(orderListPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setBackground(new Color(245,238,195));
        scroll.getViewport().setBackground(new Color(245,238,195));
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom: total + checkout
        JPanel bottom = new JPanel(new BorderLayout(0,8));
        bottom.setBackground(new Color(245,238,195));

        JSeparator sep = new JSeparator();
        sep.setForeground(GOLD);
        bottom.add(sep, BorderLayout.NORTH);

        totalLabel = new JLabel("Total: ₱0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        totalLabel.setForeground(DARK);
        bottom.add(totalLabel, BorderLayout.CENTER);

        JPanel btns = new JPanel(new GridLayout(1,2,8,0));
        btns.setBackground(new Color(245,238,195));

        JButton clearBtn = roundButton("Clear", BTN_GREY, Color.WHITE);
        clearBtn.addActionListener(e -> clearOrder());
        btns.add(clearBtn);

        JButton checkoutBtn = roundButton("Checkout  ▶", BTN_GREEN, Color.WHITE);
        checkoutBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        checkoutBtn.addActionListener(e -> {
            if (order.isEmpty()) {
                JOptionPane.showMessageDialog(this,"Please add items first!","Empty Order",JOptionPane.WARNING_MESSAGE);
            } else {
                rebuildPaymentPanel();
                cardLayout.show(mainContent,"payment");
            }
        });
        btns.add(checkoutBtn);
        bottom.add(btns, BorderLayout.SOUTH);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void addToOrder(MenuItem item) {
        order.compute(item.name(), (k,v) -> {
            if (v==null) return new int[]{item.price(),1};
            v[1]++; return v;
        });
        refreshOrderPanel();
    }

    private void clearOrder() {
        order.clear();
        refreshOrderPanel();
    }

    private void refreshOrderPanel() {
        orderListPanel.removeAll();
        int total = 0;
        for (var entry : order.entrySet()) {
            String name = entry.getKey();
            int price = entry.getValue()[0], qty = entry.getValue()[1];
            total += price * qty;
            orderListPanel.add(buildOrderRow(name, price, qty));
            orderListPanel.add(Box.createVerticalStrut(4));
        }
        totalLabel.setText("Total: ₱"+String.format("%,.2f",(double)total));
        orderListPanel.revalidate();
        orderListPanel.repaint();
    }

    private JPanel buildOrderRow(String name, int price, int qty) {
        JPanel row = new JPanel(new BorderLayout(6,0));
        row.setBackground(CARD_BG);
        row.setBorder(new CompoundBorder(
            new LineBorder(new Color(212,160,23,80),1,true),
            new EmptyBorder(6,8,6,8)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,56));

        JLabel info = new JLabel("<html><b>"+name+"</b><br>₱"+price+" × "+qty+"</html>");
        info.setFont(new Font("SansSerif", Font.PLAIN, 12));
        info.setForeground(DARK);
        row.add(info, BorderLayout.CENTER);

        JLabel sub = new JLabel("₱"+(price*qty));
        sub.setFont(new Font("SansSerif", Font.BOLD, 13));
        sub.setForeground(ACCENT);
        row.add(sub, BorderLayout.EAST);

        // qty controls
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));
        ctrl.setBackground(CARD_BG);
        JButton minus = tinyBtn("−");
        minus.addActionListener(e -> {
            int[] v = order.get(name);
            if (v!=null){ v[1]--; if(v[1]<=0) order.remove(name); }
            refreshOrderPanel();
        });
        JButton plus = tinyBtn("+");
        plus.addActionListener(e -> {
            order.compute(name,(k,v2)->{ if(v2!=null)v2[1]++; return v2; });
            refreshOrderPanel();
        });
        ctrl.add(minus); ctrl.add(plus);
        row.add(ctrl, BorderLayout.WEST);

        return row;
    }

    // ══════════════════════════════════════════════════════════
    //  PAYMENT PANEL
    // ══════════════════════════════════════════════════════════
    JPanel paymentContent;
    CardLayout payCardLayout;

    private JPanel buildPaymentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        // header
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setPaint(new GradientPaint(0,0,ACCENT,getWidth(),0,new Color(120,20,20)));
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        hdr.setPreferredSize(new Dimension(0,72));
        hdr.setBorder(new EmptyBorder(0,24,0,24));
        JLabel t = new JLabel("💳  Payment");
        t.setFont(new Font("Serif",Font.BOLD,34));
        t.setForeground(Color.WHITE);
        hdr.add(t,BorderLayout.WEST);
        JButton backBtn = roundButton("◀ Back to Menu",BTN_GREY,Color.WHITE);
        backBtn.addActionListener(e->cardLayout.show(mainContent,"menu"));
        hdr.add(backBtn,BorderLayout.EAST);
        root.add(hdr,BorderLayout.NORTH);

        payCardLayout = new CardLayout();
        paymentContent = new JPanel(payCardLayout);
        paymentContent.setBackground(BG);
        paymentContent.add(new JPanel(),"placeholder"); // filled by rebuildPaymentPanel
        root.add(paymentContent,BorderLayout.CENTER);
        return root;
    }

    private void rebuildPaymentPanel() {
        paymentContent.removeAll();
        paymentContent.add(buildPaymentSelect(),"select");
        paymentContent.add(buildGcashPanel(),"gcash");
        paymentContent.add(buildCardPanel(),"card");
        paymentContent.add(buildCashPanel(),"cash");
        paymentContent.add(buildConfirmPanel(),"confirm");
        payCardLayout.show(paymentContent,"select");
        paymentContent.revalidate();
        paymentContent.repaint();
    }

    private JPanel buildPaymentSelect() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx=0; g.gridy=GridBagConstraints.RELATIVE;
        g.insets=new Insets(16,0,16,0); g.anchor=GridBagConstraints.CENTER;

        // Order summary
        int total = order.values().stream().mapToInt(v->v[0]*v[1]).sum();
        JLabel sumLbl = new JLabel("<html><center><b>Order Total</b><br><font size=6 color='#B42828'>₱"+String.format("%,.2f",(double)total)+"</font></center></html>",SwingConstants.CENTER);
        sumLbl.setFont(new Font("Serif",Font.BOLD,22));
        p.add(sumLbl,g);

        JLabel chooseLbl = new JLabel("Choose Payment Method");
        chooseLbl.setFont(new Font("Serif",Font.BOLD,28));
        chooseLbl.setForeground(DARK);
        p.add(chooseLbl,g);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,24,0));
        btns.setBackground(BG);

        btns.add(payMethodBtn("GCash","📱\nGCash",GCASH_BLUE,e->payCardLayout.show(paymentContent,"gcash")));
        btns.add(payMethodBtn("Cash","💵\nCash",BTN_GREEN,e->payCardLayout.show(paymentContent,"cash")));
        btns.add(payMethodBtn("Card","💳\nCard",CARD_COLOR,e->payCardLayout.show(paymentContent,"card")));

        p.add(btns,g);
        return p;
    }

    private JPanel buildGcashPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx=0; g.gridy=GridBagConstraints.RELATIVE;
        g.insets=new Insets(10,0,10,0); g.anchor=GridBagConstraints.CENTER;

        JLabel hdr = new JLabel("📱  GCash Payment");
        hdr.setFont(new Font("Serif",Font.BOLD,30));
        hdr.setForeground(GCASH_BLUE);
        p.add(hdr,g);

        JLabel inst = new JLabel("Scan the QR code with your GCash app");
        inst.setFont(new Font("SansSerif",Font.PLAIN,18));
        inst.setForeground(DARK);
        p.add(inst,g);

        // QR Code drawn programmatically
        JLabel qr = new JLabel(makeQRIcon());
        p.add(qr,g);

        int total = order.values().stream().mapToInt(v->v[0]*v[1]).sum();
        JLabel amt = new JLabel("Amount: ₱"+String.format("%,.2f",(double)total));
        amt.setFont(new Font("SansSerif",Font.BOLD,20));
        amt.setForeground(ACCENT);
        p.add(amt,g);

        JLabel ref = new JLabel("GCash Ref: GC-"+String.format("%06d",(int)(Math.random()*999999)));
        ref.setFont(new Font("Monospaced",Font.PLAIN,16));
        ref.setForeground(BTN_GREY);
        p.add(ref,g);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER,16,0));
        btnRow.setBackground(BG);
        JButton back2 = roundButton("◀ Back",BTN_GREY,Color.WHITE);
        back2.addActionListener(e->payCardLayout.show(paymentContent,"select"));
        btnRow.add(back2);
        JButton confirm = roundButton("✓ Payment Done",BTN_GREEN,Color.WHITE);
        confirm.addActionListener(e->payCardLayout.show(paymentContent,"confirm"));
        btnRow.add(confirm);
        p.add(btnRow,g);
        return p;
    }

    private ImageIcon makeQRIcon() {
        int sz=220;
        BufferedImage img=new BufferedImage(sz,sz,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.fillRect(0,0,sz,sz);
        g.setColor(GCASH_BLUE);
        g.setStroke(new BasicStroke(4));
        g.drawRect(2,2,sz-4,sz-4);
        // Simulate QR pattern
        Random rnd=new Random(42);
        int cell=8, margin=16;
        int cols=(sz-margin*2)/cell, rows=(sz-margin*2)/cell;
        for(int r=0;r<rows;r++){
            for(int c=0;c<cols;c++){
                if(rnd.nextBoolean()){
                    g.fillRect(margin+c*cell,margin+r*cell,cell-1,cell-1);
                }
            }
        }
        // Corner markers
        drawQRMarker(g,margin,margin,24);
        drawQRMarker(g,sz-margin-24,margin,24);
        drawQRMarker(g,margin,sz-margin-24,24);
        // GCash label
        g.setColor(GCASH_BLUE);
        g.setFont(new Font("SansSerif",Font.BOLD,14));
        g.drawString("GCash QR",60,210);
        g.dispose();
        return new ImageIcon(img);
    }

    private void drawQRMarker(Graphics2D g, int x, int y, int sz){
        g.setColor(GCASH_BLUE); g.fillRect(x,y,sz,sz);
        g.setColor(Color.WHITE); g.fillRect(x+3,y+3,sz-6,sz-6);
        g.setColor(GCASH_BLUE); g.fillRect(x+6,y+6,sz-12,sz-12);
    }

    private JPanel buildCardPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx=0; g.gridy=GridBagConstraints.RELATIVE;
        g.insets=new Insets(12,0,12,0); g.anchor=GridBagConstraints.CENTER;

        JLabel hdr = new JLabel("💳  Card Payment");
        hdr.setFont(new Font("Serif",Font.BOLD,30));
        hdr.setForeground(CARD_COLOR);
        p.add(hdr,g);

        JLabel chooseLbl = new JLabel("Select Card Type:");
        chooseLbl.setFont(new Font("SansSerif",Font.BOLD,20));
        p.add(chooseLbl,g);

        JPanel types = new JPanel(new FlowLayout(FlowLayout.CENTER,20,0));
        types.setBackground(BG);

        ButtonGroup bg = new ButtonGroup();
        JPanel creditCard = cardTypePanel("Credit Card","💳",bg);
        JPanel debitCard  = cardTypePanel("Debit Card","🏧",bg);
        types.add(creditCard); types.add(debitCard);
        p.add(types,g);

        // Card details form
        JPanel form = new JPanel(new GridLayout(4,2,12,10));
        form.setBackground(BG);
        form.setBorder(new TitledBorder(new LineBorder(GOLD,1,true)," Card Details "));
        String[] labels={"Card Number:","Card Holder:","Expiry (MM/YY):","CVV:"};
        String[] hints={"0000-0000-0000-0000","Full Name","MM/YY","•••"};
        for(int i=0;i<labels.length;i++){
            JLabel lbl=new JLabel(labels[i]);
            lbl.setFont(new Font("SansSerif",Font.BOLD,14));
            form.add(lbl);
            JTextField tf;
            if(i==3){tf=new JPasswordField(hints[i]);}
            else{tf=new JTextField(hints[i]);}
            tf.setFont(new Font("Monospaced",Font.PLAIN,14));
            tf.setForeground(Color.GRAY);
            tf.addFocusListener(new FocusAdapter(){
                public void focusGained(FocusEvent e){if(tf.getForeground()==Color.GRAY){tf.setText("");tf.setForeground(DARK);}}
            });
            form.add(tf);
        }
        p.add(form,g);

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER,16,0));
        btnRow.setBackground(BG);
        JButton back2=roundButton("◀ Back",BTN_GREY,Color.WHITE);
        back2.addActionListener(e->payCardLayout.show(paymentContent,"select"));
        btnRow.add(back2);
        JButton pay=roundButton("✓ Pay Now",CARD_COLOR,Color.WHITE);
        pay.addActionListener(e->payCardLayout.show(paymentContent,"confirm"));
        btnRow.add(pay);
        p.add(btnRow,g);
        return p;
    }

    private JPanel cardTypePanel(String label, String emoji, ButtonGroup bg){
        JPanel p=new JPanel(new BorderLayout(0,6));
        p.setBackground(CARD_BG);
        p.setBorder(new CompoundBorder(new LineBorder(GOLD,1,true),new EmptyBorder(16,24,16,24)));
        p.setPreferredSize(new Dimension(180,120));

        JLabel ico=new JLabel(emoji,SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,38));
        p.add(ico,BorderLayout.CENTER);

        JRadioButton rb=new JRadioButton(label);
        rb.setFont(new Font("SansSerif",Font.BOLD,15));
        rb.setBackground(CARD_BG);
        rb.setHorizontalAlignment(SwingConstants.CENTER);
        bg.add(rb);
        p.add(rb,BorderLayout.SOUTH);

        p.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){rb.setSelected(true);}
            public void mouseEntered(MouseEvent e){p.setBackground(new Color(255,245,200));rb.setBackground(new Color(255,245,200));}
            public void mouseExited(MouseEvent e){p.setBackground(CARD_BG);rb.setBackground(CARD_BG);}
        });
        return p;
    }

    private JPanel buildCashPanel(){
        JPanel p=new JPanel(new GridBagLayout());
        p.setBackground(BG);
        GridBagConstraints g=new GridBagConstraints();
        g.gridx=0;g.gridy=GridBagConstraints.RELATIVE;
        g.insets=new Insets(14,0,14,0);g.anchor=GridBagConstraints.CENTER;

        JLabel hdr=new JLabel("💵  Cash Payment");
        hdr.setFont(new Font("Serif",Font.BOLD,30));
        hdr.setForeground(BTN_GREEN);
        p.add(hdr,g);

        int total=order.values().stream().mapToInt(v->v[0]*v[1]).sum();

        JLabel amtLbl=new JLabel("Amount Due: ₱"+String.format("%,.2f",(double)total));
        amtLbl.setFont(new Font("SansSerif",Font.BOLD,22));
        amtLbl.setForeground(DARK);
        p.add(amtLbl,g);

        JLabel inst=new JLabel("<html><center>Please hand cash to the cashier.<br>Wait for receipt and change.</center></html>",SwingConstants.CENTER);
        inst.setFont(new Font("SansSerif",Font.PLAIN,18));
        inst.setForeground(DARK);
        p.add(inst,g);

        // Bills illustration
        JLabel bills=new JLabel(makeBillsIcon());
        p.add(bills,g);

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER,16,0));
        btnRow.setBackground(BG);
        JButton back2=roundButton("◀ Back",BTN_GREY,Color.WHITE);
        back2.addActionListener(e->payCardLayout.show(paymentContent,"select"));
        btnRow.add(back2);
        JButton confirm=roundButton("✓ Confirm Order",BTN_GREEN,Color.WHITE);
        confirm.addActionListener(e->payCardLayout.show(paymentContent,"confirm"));
        btnRow.add(confirm);
        p.add(btnRow,g);
        return p;
    }

    private ImageIcon makeBillsIcon(){
        int w=280,h=120;
        BufferedImage img=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color[] colors={new Color(0,128,0),new Color(0,100,160),new Color(180,40,40)};
        int[][] offsets={{0,10},{30,5},{60,0}};
        for(int i=0;i<3;i++){
            g.setColor(colors[i]);
            g.fill(new RoundRectangle2D.Float(offsets[i][0]+20,offsets[i][1]+10,180,80,12,12));
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif",Font.BOLD,14));
            g.drawString("₱ PESO",offsets[i][0]+40,offsets[i][1]+55);
            g.setColor(new Color(255,255,255,60));
            g.fillOval(offsets[i][0]+140,offsets[i][1]+25,50,50);
        }
        g.dispose();
        return new ImageIcon(img);
    }

    private JPanel buildConfirmPanel(){
        JPanel p=new JPanel(new GridBagLayout());
        p.setBackground(BG);
        GridBagConstraints g=new GridBagConstraints();
        g.gridx=0;g.gridy=GridBagConstraints.RELATIVE;
        g.insets=new Insets(16,0,16,0);g.anchor=GridBagConstraints.CENTER;

        // Checkmark
        JLabel check=new JLabel("✅",SwingConstants.CENTER);
        check.setFont(new Font("Segoe UI Emoji",Font.PLAIN,90));
        p.add(check,g);

        JLabel title=new JLabel("Order Confirmed!");
        title.setFont(new Font("Serif",Font.BOLD,42));
        title.setForeground(BTN_GREEN);
        p.add(title,g);

        String ordNum="ORD-"+String.format("%04d",(int)(Math.random()*9999)+1);
        JLabel ord=new JLabel("Order Number: "+ordNum);
        ord.setFont(new Font("Monospaced",Font.BOLD,22));
        ord.setForeground(DARK);
        p.add(ord,g);

        JLabel msg=new JLabel("<html><center>Salamat sa iyong order!<br>Please wait for your number to be called.</center></html>",SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif",Font.PLAIN,18));
        msg.setForeground(DARK);
        p.add(msg,g);

        int total=order.values().stream().mapToInt(v->v[0]*v[1]).sum();
        JLabel amt=new JLabel("Total Paid: ₱"+String.format("%,.2f",(double)total));
        amt.setFont(new Font("SansSerif",Font.BOLD,20));
        amt.setForeground(ACCENT);
        p.add(amt,g);

        JButton newOrder=roundButton("🏠 New Order",ACCENT,Color.WHITE);
        newOrder.setFont(new Font("SansSerif",Font.BOLD,18));
        newOrder.setPreferredSize(new Dimension(200,50));
        newOrder.addActionListener(e->{
            order.clear();
            refreshOrderPanel();
            cardLayout.show(mainContent,"welcome");
        });
        p.add(newOrder,g);
        return p;
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════
    private JButton payMethodBtn(String id, String label, Color bg, ActionListener al){
        String[] parts=label.split("\n");
        JButton btn=new JButton("<html><center><font size=6>"+parts[0]+"</font><br><br>"+parts[1]+"</center></html>");
        btn.setFont(new Font("SansSerif",Font.BOLD,20));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(200,150));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(bg.darker(),2,true),
            new EmptyBorder(16,24,16,24)
        ));
        btn.addActionListener(al);
        btn.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){btn.setBackground(bg.brighter());}
            public void mouseExited(MouseEvent e){btn.setBackground(bg);}
        });
        return btn;
    }

    static JButton roundButton(String text, Color bg, Color fg){
        JButton b=new JButton(text);
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(new Font("SansSerif",Font.BOLD,14));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setBorder(new CompoundBorder(
            new LineBorder(bg.darker(),1,true),
            new EmptyBorder(8,18,8,18)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setBackground(bg.darker());}
            public void mouseExited(MouseEvent e){b.setBackground(bg);}
        });
        return b;
    }

    static JButton tinyBtn(String t){
        JButton b=new JButton(t);
        b.setFont(new Font("SansSerif",Font.BOLD,14));
        b.setBackground(new Color(200,180,120));
        b.setForeground(DARK);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(28,28));
        b.setBorder(new EmptyBorder(0,0,0,0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ══════════════════════════════════════════════════════════
    //  WrapLayout — wraps JPanel children like CSS flex-wrap
    // ══════════════════════════════════════════════════════════
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align,int hgap,int vgap){super(align,hgap,vgap);}
        @Override public Dimension preferredLayoutSize(Container target){return layoutSize(target,true);}
        @Override public Dimension minimumLayoutSize(Container target){return layoutSize(target,false);}
        private Dimension layoutSize(Container target,boolean pref){
            synchronized(target.getTreeLock()){
                int tw=target.getWidth();
                if(tw==0) tw=Integer.MAX_VALUE;
                Insets ins=target.getInsets();
                tw-=ins.left+ins.right+getHgap()*2;
                int rowW=0,rowH=0,totalH=ins.top+ins.bottom+getVgap()*2;
                boolean first=true;
                for(Component c:target.getComponents()){
                    if(!c.isVisible()) continue;
                    Dimension d=pref?c.getPreferredSize():c.getMinimumSize();
                    if(!first&&rowW+d.width+getHgap()>tw){
                        totalH+=rowH+getVgap(); rowW=0; rowH=0;
                    }
                    rowW+=d.width+getHgap(); rowH=Math.max(rowH,d.height); first=false;
                }
                totalH+=rowH;
                return new Dimension(tw+ins.left+ins.right,totalH);
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    public static void main(String[] args){
        try{ UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch(Exception ignored){}
        SwingUtilities.invokeLater(SaloSaloKiosk::new);
    }
}