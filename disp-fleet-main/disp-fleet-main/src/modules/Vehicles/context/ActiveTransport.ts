import { useCallback, useState } from 'react';

import { Transport } from 'api/transport/transport.types';
import { useTransport } from 'api/transport/transport.api';
import { createCallableCtx } from 'utils/createCallableContext';
import { ignore } from 'utils/utils';

const useHook = () => {
  const [activeTransport, setActiveTransport] = useState<Transport | null>(null);

  const [getTransport, { isLoading }] = useTransport();

  const update = useCallback((transport?: Transport) => {
    const selectedTransoport = transport ?? activeTransport;

    if (selectedTransoport) {
      getTransport(selectedTransoport.id)
        .then(transport => {
          if (transport) {
            setActiveTransport(transport);
          }
        })
        .catch(ignore);
    }
  }, [activeTransport, getTransport]);

  const toggleActiveTransport = useCallback(
    (transport: Transport) => {
      if (activeTransport?.id === transport?.id) {
        setActiveTransport(null);
        return;
      }

      if (transport && !isLoading) {
        update(transport);
      }
    },
    [activeTransport, isLoading, update]
  );

  return {
    activeTransport,
    isTransportLoading: isLoading,
    setActiveTransport,
    toggleActiveTransport,
    update,
  };
};

export const [useActiveTransport, ActiveTransportProvider] = createCallableCtx(useHook, {
  name: 'ActiveTransportProvider',
});
