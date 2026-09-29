import { ReactPortal } from 'react';
import ReactDOM from 'react-dom';

export const rootElement: Element = (document as Document).querySelector('#root') as Element;

export const createJsxPortal: (component: JSX.Element) => ReactPortal = (component: JSX.Element): ReactPortal => ReactDOM.createPortal(component, rootElement);
