import * as t from 'io-ts';
import type { AxiosResponse } from 'axios';
import {
  useCallback,
  useEffect,
  useRef,
  useState
} from 'react';
import moment from 'moment';

import { useAppStore } from 'ioc';
import { useProfile } from 'api/profile/profile.api';
import { useToken } from 'hooks/useToken';
import { useTranslation } from 'i18n';
import { WEBSOCKET_URL } from 'constants/api.constants';

export enum WEBSOCKET_CONNECTION_STATUS {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
}

export enum PingPong {
  Ping = 'PING',
  Pong = 'PONG',
}

export enum WEBSOCKET_CODES {
  UNMOUNT_CLOSE_CODE = 4000,
  INCORRECT_TOKEN = 4001,
  NO_PONG = 4002,
}

const MAX_RECONNECTIONS = 2;
const PING_PONG_TIME = 20000;
const PONG_WAITING_TIME = 3000;

interface LastMessage<T = unknown> {
  data: T;
  time: string;
}

export const useWebsocket = <TResponse = unknown, TSend = unknown>(
  route: string,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  type: any = t.unknown,
  { errorMessage }: { errorMessage?: string } = {}
) => {
  const [websocket, setWebsocket] = useState<WebSocket | undefined>();
  const [isOpened, setIsOpened] = useState(false);
  const [connectionCount, setConnectionCount] = useState(0);
  const [lastMessage, setLastMessage] = useState<LastMessage<TResponse>>();

  const reconnections = useRef(0);

  const { logger, process } = useAppStore();
  const { id } = useProfile().data;
  const token = useToken();
  const { t: labels } = useTranslation();

  const { authStore } = useAppStore();

  useEffect(() => {
    const url = WEBSOCKET_URL + route;

    const _websocket = new WebSocket(`${url}/${id}/`);

    setWebsocket(_websocket);

    return () => {
      _websocket.close(WEBSOCKET_CODES.UNMOUNT_CLOSE_CODE, 'Component was unmounted');
      setWebsocket(undefined);
    };
  }, [route, id, connectionCount]);

  useEffect(() => {
    if (!websocket || !token) return;

    websocket.onopen = async () => {
      const config = await authStore.checkAuth();
      const [, newToken] = config.headers.Authorization.split(' ');
      websocket.send(newToken);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [websocket, token]);

  useEffect(() => {
    if (!websocket) return;

    let keepConnectionTimer: NodeJS.Timeout;
    let pongAwaitingTimer: NodeJS.Timeout;

    const keepConnection = () => {
      websocket.send(PingPong.Ping);
      pongAwaitingTimer = setTimeout(() => websocket.close(WEBSOCKET_CODES.NO_PONG), PONG_WAITING_TIME);
    };

    websocket.onmessage = (e: MessageEvent<string>) => {
      switch (e.data) {
        case WEBSOCKET_CONNECTION_STATUS.SUCCESS: {
          reconnections.current = 0;
          setIsOpened(true);
          keepConnectionTimer = setTimeout(keepConnection, PING_PONG_TIME);
          break;
        }

        case WEBSOCKET_CONNECTION_STATUS.FAILED: {
          websocket.close(WEBSOCKET_CODES.INCORRECT_TOKEN);
          break;
        }

        case PingPong.Pong: {
          keepConnectionTimer = setTimeout(keepConnection, PING_PONG_TIME);
          clearTimeout(pongAwaitingTimer);
          break;
        }

        default: {
          let response: TResponse;
          try {
            response = JSON.parse(e.data);
          } catch {
            response = e.data as unknown as TResponse;
          }

          response = process.decodeResponseData<TResponse>(type)({
            data: response,
          } as unknown as AxiosResponse<TResponse>);

          setLastMessage({
            data: response,
            time: moment().format(),
          });

          return response;
        }
      }
    };

    websocket.onclose = (e: CloseEvent) => {
      clearTimeout(keepConnectionTimer);
      setIsOpened(false);

      if (e.code === WEBSOCKET_CODES.UNMOUNT_CLOSE_CODE) return;

      if (reconnections.current < MAX_RECONNECTIONS) {
        reconnections.current += 1;
        setTimeout(() => setConnectionCount(prev => prev + 1), reconnections.current * 1000);
      } else {
        logger.toNotify(
          'error',
          errorMessage ?? labels.Websockets.autorefreshUnavaliable,
          `${labels.Websockets.connectionError} [${e.code}]`
        );
      }
    };

    return () => {
      clearTimeout(keepConnectionTimer);
      clearTimeout(pongAwaitingTimer);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [websocket]);

  const sendMessage = useCallback(
    (data: TSend) => websocket?.send(typeof data === 'string' ? data : JSON.stringify(data)),
    [websocket]
  );

  return {
    isOpened,
    lastMessage,
    sendMessage,
  };
};
