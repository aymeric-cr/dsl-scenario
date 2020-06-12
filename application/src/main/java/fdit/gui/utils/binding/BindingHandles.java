package fdit.gui.utils.binding;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

import java.util.function.BiConsumer;

public final class BindingHandles {

    private BindingHandles() {
    }

    public static <T> BindingHandle createDetachBinding(final Property<T> from,
                                                        final ObservableValue<T> to) {
        return new BindingHandle() {
            @Override
            public void bind() {
                from.bind(to);
            }

            @Override
            public void unbind() {
                from.unbind();
            }
        };
    }

    public static <T> BindingHandle createAttachBinding(final Property<T> from,
                                                        final ObservableValue<T> to) {
        final BindingHandle detachBinding = createDetachBinding(from, to);
        detachBinding.bind();
        return detachBinding;
    }

    public static <T> BindingHandle createDetachBindingBidirectional(final Property<T> from,
                                                                     final Property<T> to) {
        return new BindingHandle() {
            @Override
            public void bind() {
                from.bindBidirectional(to);
            }

            @Override
            public void unbind() {
                from.unbindBidirectional(to);
            }
        };
    }

    public static <T> BindingHandle createAttachBindingBidirectional(final Property<T> from,
                                                                     final Property<T> to) {
        final BindingHandle detachBindingBidirectional = createDetachBindingBidirectional(from, to);
        detachBindingBidirectional.bind();
        return detachBindingBidirectional;
    }

    public static BindingHandleBuilder createBindingHandle(final Property from,
                                                           final Property to) {
        return new BindingHandleBuilder(from, to);
    }

    public static class BindingHandleBuilder {

        private final Property from;
        private final Property to;
        private BiConsumer<Property, Property> onAttachConsumer;
        private BiConsumer<Property, Property> onDetachConsumer;

        private BindingHandleBuilder(final Property from,
                                     final Property to) {

            this.from = from;
            this.to = to;
        }

        public BindingHandleBuilder onBind(final BiConsumer<Property, Property> onAttachConsumer) {
            this.onAttachConsumer = onAttachConsumer;
            return this;
        }

        public BindingHandleBuilder onUnbind(final BiConsumer<Property, Property> onDetachConsumer) {
            this.onDetachConsumer = onDetachConsumer;
            return this;
        }

        public BindingHandle buildAttach() {
            if (onAttachConsumer == null || onDetachConsumer == null) {
                throw new RuntimeException("Can't build BindingHandle without onAttach or onDetach action");
            }
            return new BindingHandle() {
                @Override
                public void bind() {
                    onAttachConsumer.accept(from, to);
                }

                @Override
                public void unbind() {
                    onDetachConsumer.accept(from, to);
                }
            };
        }
    }
}
