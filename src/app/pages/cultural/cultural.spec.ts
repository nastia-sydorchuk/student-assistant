import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Cultural } from './cultural';

describe('Cultural', () => {
  let component: Cultural;
  let fixture: ComponentFixture<Cultural>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cultural]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Cultural);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
