import { message } from 'antd';

import { IUseCardProps } from './Card';

export const useCard = ({
  name,
}: IUseCardProps): {
    onInfoClick: () => void;
  } => {
  function onInfoClick(): void {
    message.warning(`${name} не доступен для текущего пользователя`);
  }

  return { onInfoClick };
};
