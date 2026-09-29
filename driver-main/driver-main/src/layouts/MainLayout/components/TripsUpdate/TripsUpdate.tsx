import { FC } from 'react';
import { useTripsUpdate } from 'layouts/MainLayout/hooks/useTripsUpdate';

const TripsUpdate: FC = () => {
  useTripsUpdate();

  return null;
};

export default TripsUpdate;
