package org.au.tonomy.client.widget;

import org.au.tonomy.client.bus.Bus;
import org.au.tonomy.client.bus.Message;
import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.IUndo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class MessageListWidget extends Composite {

  private static IMessageListWidgetUiBinder BINDER = GWT.create(IMessageListWidgetUiBinder.class);
  interface IMessageListWidgetUiBinder extends UiBinder<Widget, MessageListWidget> { }

  @UiField FlowPanel panel;
  private IUndo undoMessageListener;
  private int messageCount = 0;

  public MessageListWidget() {
    initWidget(BINDER.createAndBindUi(this));
    updateMessageCount(0);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    panel.clear();
    Bus bus = Bus.get();
    undoMessageListener = bus.addMessageAddedListener(new IThunk<Message>() {
      @Override
      public void call(Message value) {
        addMessage(value);
      }
    });
    for (Message message : bus.getMessages())
      addMessage(message);
  }

  private void addMessage(Message message) {
    final MessageWidget widget = new MessageWidget();
    widget.setText(message.getText());
    panel.add(widget);
    message.addListener(new Message.IListener() {
      @Override
      public void onDeleted(Message message) {
        removeMessage(widget);
      }
      @Override
      public void onChanged(Message message) {
        widget.setText(message.getText());
        widget.setWeight(message.getWeight());
      }
    });
    updateMessageCount(1);
  }

  private void removeMessage(MessageWidget widget) {
    panel.remove(widget);
    updateMessageCount(-1);
  }

  private void updateMessageCount(int delta) {
    messageCount += delta;
    if (messageCount == 0) {
      panel.getElement().getStyle().setVisibility(Visibility.HIDDEN);
    } else if (messageCount == 1) {
      panel.getElement().getStyle().setVisibility(Visibility.VISIBLE);
    }
  }

  @Override
  protected void onUnload() {
    if (undoMessageListener != null) {
      undoMessageListener.undo();
      undoMessageListener = null;
    }
    super.onUnload();
  }

}
