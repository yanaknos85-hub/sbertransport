import Toast, { ToastShowProps } from 'antd-mobile/es/components/toast';
import { ToastProps } from 'antd-mobile/es/components/toast/toast';

export const logger = (type: ToastProps['icon'], content: string, config?: ToastShowProps) => {
  Toast.clear();

  return Toast.show({
    content,
    icon: type,
    position: 'top',
    ...config,
  });
};
