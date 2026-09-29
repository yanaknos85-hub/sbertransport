import * as tt from 'utils/io-ts';

/*
 * Fast UUID generator, RFC4122 version 4 compliant.
 * @author Jeff Ward (jcward.com).
 * @license MIT license
 * @link http://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid-in-javascript/21963136#21963136
 */

/* eslint-disable prefer-template, no-bitwise */

const lut = new Array(256).fill(0).map((_, i) => `${i < 0x10 ? '0' : ''}${i.toString(16)}`);

export default function (): tt.UUID {
  const d0 = (Math.random() * 0xffffffff) | 0;
  const d1 = (Math.random() * 0xffffffff) | 0;
  const d2 = (Math.random() * 0xffffffff) | 0;
  const d3 = (Math.random() * 0xffffffff) | 0;

  return (lut[d0 & 0xff]
    + lut[(d0 >> 8) & 0xff]
    + lut[(d0 >> 16) & 0xff]
    + lut[(d0 >> 24) & 0xff]
    + '-'
    + lut[d1 & 0xff]
    + lut[(d1 >> 8) & 0xff]
    + '-'
    + lut[((d1 >> 16) & 0x0f) | 0x40]
    + lut[(d1 >> 24) & 0xff]
    + '-'
    + lut[(d2 & 0x3f) | 0x80]
    + lut[(d2 >> 8) & 0xff]
    + '-'
    + lut[(d2 >> 16) & 0xff]
    + lut[(d2 >> 24) & 0xff]
    + lut[d3 & 0xff]
    + lut[(d3 >> 8) & 0xff]
    + lut[(d3 >> 16) & 0xff]
    + lut[(d3 >> 24) & 0xff]) as tt.UUID;
}
