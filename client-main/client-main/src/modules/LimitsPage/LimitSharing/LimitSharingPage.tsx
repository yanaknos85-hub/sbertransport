import { FC } from 'react';
import { Limit } from 'stores/Limits/Limit.interface';

interface LimitSharingPageProps {
  limit: Limit;
  getLimit: () => void;
}

const LimitSharingPage: FC<LimitSharingPageProps> = () => null;

export default LimitSharingPage;
