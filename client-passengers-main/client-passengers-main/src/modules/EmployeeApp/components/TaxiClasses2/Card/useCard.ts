/* eslint-disable @typescript-eslint/no-explicit-any */
import { message } from 'antd';

export const useCard = ({
  name,
}: any): {
    onInfoClick: () => void;
  } => {
  function onInfoClick(): void {
    message.warning(`${name} не доступен для текущего пользователя`);
  }

  return { onInfoClick };
};
