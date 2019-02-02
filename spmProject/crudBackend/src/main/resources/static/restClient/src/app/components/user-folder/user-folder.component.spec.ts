import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserFolderComponent } from './user-folder.component';

describe('UserFolderComponent', () => {
  let component: UserFolderComponent;
  let fixture: ComponentFixture<UserFolderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserFolderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserFolderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
