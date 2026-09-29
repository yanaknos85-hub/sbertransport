import { notification } from 'antd';
import React from 'react';

/**
 * уведомление с ссылкой
 */
export const showNotification = (options: {
  type?: 'success' | 'error' | 'info' | 'warning';
  message: string;
  description: string;
  link?: { url: string; text?: string };
  duration?: number;
  placement?: 'top' | 'topLeft' | 'topRight' | 'bottom' | 'bottomLeft' | 'bottomRight';
}) => {
  const {
    type = 'info',
    message,
    description,
    link,
    duration = 4.5,
    placement = 'top',
  } = options;

  const config: any = {
    message,
    duration,
    placement,
  };

  if (link) {
    config.description = React.createElement(
      'div',
      null,
      React.createElement('p', null, description),
      React.createElement(
        'a',
        {
          href: link.url,
          style: {
            marginTop: '8px', display: 'inline-block', color: 'rgb(16, 191, 106)',
          },
        },
        ` ${link.text || 'Перейти'}`
      )
    );
  } else {
    config.description = React.createElement('p', null, description);
  }

  if (notification[type]) {
    notification[type](config);
  } else {
    notification.open(config);
  }
};

export const notificationService = {
  success: (message: string, description: string, linkOptions?: { url: string; text?: string }) => showNotification({
    type: 'success',
    message,
    description,
    link: linkOptions,
  }),

  error: (message: string, description: string, linkOptions?: { url: string; text?: string }) => showNotification({
    type: 'error',
    message,
    description,
    link: linkOptions,
  }),

  info: (message: string, description: string, linkOptions?: { url: string; text?: string }) => showNotification({
    type: 'info',
    message,
    description,
    link: linkOptions,
  }),

  warning: (message: string, description: string, linkOptions?: { url: string; text?: string }) => showNotification({
    type: 'warning',
    message,
    description,
    link: linkOptions,
  }),
};
