import React, { useCallback, useEffect, useState } from 'react';
import { Popup } from './Popup';
import styled from 'styled-components';
import Button from 'shared/ui/Button/Button';
import { usePlatformDetect } from 'shared/hooks/usePlatformDetect';

const Root = styled.div`
  display: flex;
  gap: 16px;
`;

const Container = styled.div`
  display: flex;
  flex-direction:column;
  align-items: start;
  gap: 24px;
`;

const Logo = styled.div`
  border-radius: 8px;
  box-shadow: rgba(0, 0, 0, 0.13) 0px 0px 12px 1px;
  width: 64px;
  height: 64px;
  background-size: contain;
  background-image: url('/assets/favicon/android-chrome-512x512.png');
`;

const Text = styled.div`
  color: rgb(38, 38, 38);
  font-family: SB Sans Text;
  font-size: 16px;
  line-height: 22px;
  letter-spacing: -0.3px;
`;

export const InstallPwa = () => {
  const [deferredEvent, setDeferredEvent] = useState(null);

  const [isInstallOpen, setIsInstallOpen] = useState(false);

  useEffect(() => {
    const beforeInstallPrompt = event => {
      event.preventDefault();

      setDeferredEvent(event);
    };

    window.addEventListener('beforeinstallprompt', beforeInstallPrompt);

    return () => {
      window.removeEventListener('beforeinstallprompt', beforeInstallPrompt);
    };
  }, []);

  useEffect(() => {
    if (deferredEvent) {
      setIsInstallOpen(true);
    }
  }, [deferredEvent]);

  const onInstall = useCallback(() => {
    if (deferredEvent) {
      deferredEvent.prompt();
    }
  }, [deferredEvent]);

  const onClose = useCallback(() => {
    setIsInstallOpen(false);
  }, []);

  const { isMobile } = usePlatformDetect();

  return (
    <>
      {isMobile && isInstallOpen
      && (
        <Popup topOffset={10} onClose={onClose}>
          <Root>
            <Logo />
            <Container>
              <Text>
                Установить "СберТранспорт"
                <br />
                на экран телефона
              </Text>
              <Button
                $size="small"
                onClick={onInstall}
                style={{
                  backgroundColor: '#F2F3F6',
                  color: '#4D4D4D',
                  borderColor: '#F2F3F6',
                  padding: '0 20px',
                  fontFamily: 'SB Sans Text',
                  fontSize: '16px',
                  fontWeight: 600,
                  lineHeight: '18px',
                  letterSpacing: '-1.3px',
                }}
              >
                Установить
              </Button>
            </Container>
          </Root>
        </Popup>
      )}
    </>
  );
};
