/* eslint-disable @typescript-eslint/no-explicit-any */
interface Dictionary<T> {
  [index: string]: T;
}

type PartialBy<T, K extends keyof T> = Omit<T, K> & Partial<Pick<T, K>>;

type Override<T, O> = Omit<T, keyof O> & O;

type FormItem<C extends React.ComponentType<{ value: any; onChange: any } & any>> = React.ComponentType<
  Omit<React.ComponentProps<C>, 'value' | 'onChange'>
>;

module 'leaflet.locatecontrol' {
  class Locate {
    constructor(options: { position: 'bottomright' | 'topleft' | 'topright' | 'bottomleft' });

    addTo: (map?: Map) => void;

    start: () => void;
  }
  export default Locate;
}

declare module 'auth/*';