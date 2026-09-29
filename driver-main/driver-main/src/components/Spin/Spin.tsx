import { AutoCenter, SpinLoading } from 'antd-mobile';
import { FC } from 'react';

const Spin: FC = () => {
  return (
    <AutoCenter>
      <SpinLoading />
    </AutoCenter>
  );
};

export default Spin;
