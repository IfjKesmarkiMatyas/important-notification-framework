import { emptyKit } from '../../models';
import { STRINGS } from '../../core/i18n/strings';
import { KitEditor } from './kit-editor';

describe('KitEditor', () => {
  function editor(): KitEditor {
    const component = new KitEditor();
    component.kit = emptyKit();
    component.t = STRINGS.hu;
    return component;
  }

  it('adds disaster, market and breaking interests', () => {
    const component = editor();
    component.addInterest('disaster');
    component.addInterest('market');
    component.addInterest('breaking');
    expect(component.kit.interests.map((item) => item.type)).toEqual(['disaster', 'market', 'breaking']);
    expect(component.kit.interests[0].minMagnitude).toBe(6);
  });

  it('parses and serializes breaking topics', () => {
    const component = editor();
    component.addInterest('breaking');
    component.setTopics(0, 'belfold,  kulfold ,');
    expect(component.kit.interests[0].topics).toEqual(['belfold', 'kulfold']);
    expect(component.topicsText(0)).toBe('belfold, kulfold');
  });

  it('removes an interest by index', () => {
    const component = editor();
    component.addInterest('breaking');
    component.addInterest('market');
    component.removeInterest(0);
    expect(component.kit.interests).toHaveSize(1);
    expect(component.kit.interests[0].type).toBe('market');
  });

  it('toggles channel sliders and walks the wizard', () => {
    const component = editor();
    expect(component.kit.preferences.channels.pushover).toBeFalse();
    component.toggleChannel('pushover');
    expect(component.kit.preferences.channels.pushover).toBeTrue();
    component.next();
    expect(component.step).toBe(2);
    component.next();
    expect(component.step).toBe(3);
    component.back();
    expect(component.step).toBe(2);
  });

  it('labels rule when keys in the form language', () => {
    const component = editor();
    expect(component.whenLabel('disaster.earthquake.minMagnitude')).toBe('Földrengés magnitúdó');
    expect(component.whenLabel('market.movePercent')).toBe('Piaci elmozdulás');
    expect(component.whenLabel('breaking')).toBe('Breaking hír');
  });
});
