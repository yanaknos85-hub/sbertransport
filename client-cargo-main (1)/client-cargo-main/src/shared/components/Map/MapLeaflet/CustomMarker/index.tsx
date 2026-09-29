import React from 'react';
import { Marker, MarkerProps } from 'react-leaflet';
import { MarkerOptions } from 'leaflet';

export type CustomMarker = MarkerProps & MarkerOptions;
export const CustomMarker = ({ ...props }: CustomMarker): JSX.Element => <Marker {...props} />;
