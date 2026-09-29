import { Dispatch, SetStateAction, useState } from 'react';

import { Transport } from 'stores/Trip/Trip.interface';

export const useTransport = (
  initTransport?: Transport | undefined
): {
  transport: Transport | undefined;
  setTransport: Dispatch<SetStateAction<Transport | undefined>>;
} => {
  const [transport, setTransport] = useState<Transport | undefined>(initTransport);

  return {
    transport,
    setTransport,
  };
};
