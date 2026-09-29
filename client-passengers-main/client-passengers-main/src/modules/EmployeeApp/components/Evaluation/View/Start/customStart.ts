import { CSSProperties } from 'react';

const customStart: Record<string, CSSProperties> = {
  tBar: {
    height: '100px',
    fontSize: '12px',
    lineHeight: '14px',
    letterSpacing: '-0.132px',
    margin: '0 0 24px 0',
  },
  tTab: {
    width: '97px',
    justifyContent: 'space-between',
    paddingTop: '10px',
  },
  p: {
    height: '32px',
    lineHeight: '14px',
    textAlign: 'center',
  },
  textFieldContainer: {
    height: '86px',
    width: '100%',
    marginBottom: 32,
  },
  textField: {
    width: '100%',
    height: 86,
    border: '1px solid #E0E0E0',
    borderRadius: 8,
    fontWeight: 400,
    fontSize: 16,
    color: '#909090',
    padding: 16,
    verticalAlign: 'top',
    resize: 'none',
    outline: 'none',
  },
  symbolLimit: {
    width: 460,
    height: 22,

    margin: '8px 0 0 0',

    fontFamily: 'SB Sans Text',
    fontStyle: 'normal',
    fontWeight: 400,
    fontSize: 14,
    lineHeight: '22px',

    textAlign: 'right',
    letterSpacing: '-0.3px',

    color: '#4D4D4D',

    flex: 'none',
    order: 0,
    flexGrow: 1,
  },
};

export default customStart;
