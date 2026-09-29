import {
  useCallback, useEffect, useRef, useState
} from 'react';
import * as t from 'io-ts';
import { AxiosResponse } from 'axios';
import dayjs from 'dayjs';
import { useAppStore } from 'stores/stores.context';
import { logger } from 'utils/logger/logger';
import { useProfile } from './services/DispatcherRoom/DispatcherRoom.query';
import { PingPong, WEBSOCKET_CONNECTION_STATUS, WEBSOCKET_URL } from './constants';

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

const useWebsocket = <TResponse = unknown, TSend = unknown>(
  route: string,
  type: t.Any = t.unknown,
  { errorMessage }: { errorMessage?: string } = {}
) => {
  const [websocket, setWebsocket] = useState<WebSocket>();
  const [isOpened, setIsOpened] = useState(false);
  const [connectionCount, setConnectionCount] = useState(0);
  const [lastMessage, setLastMessage] = useState<LastMessage<TResponse>>();

  const reconnections = useRef(0);

  const { process, authStore } = useAppStore();
  const { id } = useProfile().data;

  const { token } = authStore;

  useEffect(() => {
    const url = WEBSOCKET_URL + route;

    try {
      const _websocket = new WebSocket(`${url}/${id}/`);

      setWebsocket(_websocket);

      return () => {
        _websocket.close(WEBSOCKET_CODES.UNMOUNT_CLOSE_CODE, 'Component was unmounted');
        setWebsocket(undefined);
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }, [route, id, connectionCount]);

  useEffect(() => {
    if (!websocket || !token) return;

    websocket.onopen = async () => {
      const config = await authStore.checkAuth();
      const [, newToken] = config.headers.Authorization.split(' ');
      websocket.send(newToken);
    };
  }, [websocket, token, authStore]);

  useEffect(() => {
    if (!websocket) return;

    let keepConnectionTimer: NodeJS.Timeout,
      pongAwaitingTimer: NodeJS.Timeout;

    const keepConnection = () => {
      websocket.send(PingPong.Ping);
      pongAwaitingTimer = setTimeout(() => websocket.close(WEBSOCKET_CODES.NO_PONG), PONG_WAITING_TIME);
    };

    websocket.onmessage = e => {
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
            response = e.data;
          }

          response = process.decodeResponseData<TResponse>(type)({
            data: response,
          } as unknown as AxiosResponse<TResponse>);

          setLastMessage({
            data: response,
            time: dayjs().format(),
          });

          return response;
        }
      }
    };

    websocket.onclose = e => {
      clearTimeout(keepConnectionTimer);
      setIsOpened(false);

      if (e.code === WEBSOCKET_CODES.UNMOUNT_CLOSE_CODE) return;

      if (reconnections.current < MAX_RECONNECTIONS) {
        reconnections.current++;
        setTimeout(() => setConnectionCount(prev => prev + 1), reconnections.current * 1000);
      } else {
        logger(
          'error',
          `${errorMessage ?? 'Ошибка подключения к вебсокетам'} [${e.code}]`
        );
      }
    };

    return () => {
      clearTimeout(keepConnectionTimer);
      clearTimeout(pongAwaitingTimer);
    };
  }, [errorMessage, process, type, websocket]);

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

export default useWebsocket;
