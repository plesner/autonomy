package org.au.tonomy.client.widget;

import org.au.tonomy.client.bus.Bus;
import org.au.tonomy.client.bus.Message;
import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.IUndo;

import com.google.gwt.core.client.GWT;
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

  public MessageListWidget() {
    initWidget(BINDER.createAndBindUi(this));
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
        panel.remove(widget);
      }
      @Override
      public void onChanged(Message message) {
        widget.setText(message.getText());
        widget.setWeight(message.getWeight());
      }
    });
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
