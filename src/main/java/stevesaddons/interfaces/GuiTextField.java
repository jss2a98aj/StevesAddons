package stevesaddons.interfaces;

import java.util.Timer;
import java.util.TimerTask;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public class GuiTextField extends Gui
{
    private class ToggleCursor extends TimerTask
    {
        @Override
        public void run()
        {
            toggleCursor = !toggleCursor;
        }
    }

    private int xSize, ySize;
    private int x, y;
    private String text;
    private FontRenderer fontRenderer;
    private int cursorPos = 0;
    private boolean toggleCursor;
    private Timer timer = new Timer();

    public GuiTextField(int width, int height, int x, int y)
    {
        this.x = x;
        this.y = y;
        this.xSize = width;
        this.ySize = height;
        this.text = "";
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.timer.scheduleAtFixedRate(new ToggleCursor(), 0, 300);
    }

    protected void setText(String text)
    {
        this.text = text;
    }

    protected void fixCursorPos()
    {
        cursorPos = text.length();
    }

    public String getText()
    {
        return this.text;
    }

    public void keyTyped(char c, int keycode)
    {
        if (Character.isLetterOrDigit(c))
        {
            if (cursorPos != this.text.length())
            {
                setText(this.text.substring(0, cursorPos) + c + this.text.substring(cursorPos));
            } else
            {
                setText(this.text + c);
            }

            cursorPos++;
        } else
        {
            switch (keycode)
            {
                case Keyboard.KEY_BACK:
                    if (this.text.length() > 0)
                    {
                        if (cursorPos == this.text.length())
                        {
                            setText(this.text.substring(0, cursorPos - 1));
                        } else if (cursorPos > 1)
                        {
                            setText(this.text.substring(0, cursorPos - 1) + this.text.substring(cursorPos));
                        } else if (cursorPos == 1)
                        {
                            setText(this.text.substring(1));
                        }

                        if (cursorPos > 0)
                        {
                            cursorPos--;
                        }
                    }
                    break;
                case Keyboard.KEY_DELETE:
                    if (this.text.length() > 0)
                    {
                        if (cursorPos == 0)
                        {
                            setText(this.text.substring(1));
                        } else if (cursorPos > 0 && cursorPos < this.text.length())
                        {
                            setText(this.text.substring(0, cursorPos) + this.text.substring(cursorPos + 1));
                        }
                    }
                    break;
                case Keyboard.KEY_SPACE:
                    if (cursorPos <= this.text.length())
                    {
                        cursorPos++;
                    }
                    setText(this.text + " ");
                    break;
                case Keyboard.KEY_LEFT:
                    if (cursorPos > 0)
                    {
                        cursorPos--;
                    }
                    break;
                case Keyboard.KEY_RIGHT:
                    if (cursorPos < this.text.length())
                    {
                        cursorPos++;
                    }
                    break;
                case Keyboard.KEY_HOME:
                    cursorPos = 0;
                    break;
                case Keyboard.KEY_END:
                    cursorPos = this.text.length();
                    break;
                default:
                    break;
            }
        }
    }

    public void draw()
    {
        drawBackground();
        drawText();
    }

    private void drawBackground()
    {
        drawRect(this.x - 1, this.y - 1, this.x + xSize + 1, this.y + ySize + 1, 0xffcf191f);
        drawRect(this.x, this.y, this.x + xSize, this.y + ySize, 0xff000000);
    }

    public String getPreCursor()
    {
        return getText().substring(0, cursorPos);
    }

    public String getPostCursor()
    {
        return getText().substring(cursorPos);
    }

    public void drawText()
    {
        String preCursor = getPreCursor();
        fontRenderer.drawString(preCursor, this.x + 2, this.y + ySize / 2 - 4, 0xe0e0e0);
        int x = this.x + 2 + fontRenderer.getStringWidth(preCursor);
        if (toggleCursor)
            drawRect(x, this.y + ySize / 2 - 4, x + 1, this.y + ySize / 2 + 4, 0xffe0e0e0);
        fontRenderer.drawString(getPostCursor(), x + 2, this.y + ySize / 2 - 4, 0xe0e0e0);
    }

    public void close()
    {
        this.timer.cancel();
    }
}
