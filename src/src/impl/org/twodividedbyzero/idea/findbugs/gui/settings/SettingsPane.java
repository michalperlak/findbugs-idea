/*
 * Copyright 2016 Andre Pfeiler
 *
 * This file is part of FindBugs-IDEA.
 *
 * FindBugs-IDEA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FindBugs-IDEA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FindBugs-IDEA.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.twodividedbyzero.idea.findbugs.gui.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.core.AbstractSettings;
import org.twodividedbyzero.idea.findbugs.core.ProjectSettings;
import org.twodividedbyzero.idea.findbugs.core.WorkspaceSettings;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

abstract class SettingsPane extends JPanel implements Disposable {

	private GeneralTab generalTab;
	private ReportTab reportTab;
	private FilterTab filterTab;
	private DetectorTab detectorTab;
	private AnnotateTab annotateTab;
	private ShareTab shareTab;

	SettingsPane(@NotNull final Project project) {
		super(new BorderLayout());

		generalTab = createGeneralTab();
		reportTab = new ReportTab();
		filterTab = new FilterTab();
		detectorTab = new DetectorTab(project);
		annotateTab = createAnnotateTab();
		shareTab = createShareTab();

		final JPanel topPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topPane.add(createToolbar().getComponent());
		topPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		add(topPane, BorderLayout.NORTH);

		/**
		 * LATER: Switch to TabbedPaneWrapper after
		 * https://github.com/JetBrains/intellij-community/pull/398
		 */
		//final TabbedPaneWrapper tabs = new TabbedPaneWrapper(this);
		final JBTabbedPane tabs = new JBTabbedPane();
		if (generalTab != null) {
			tabs.addTab(ResourcesLoader.getString("settings.general"), generalTab);
		}
		tabs.addTab(ResourcesLoader.getString("settings.report"), reportTab);
		tabs.addTab(ResourcesLoader.getString("settings.filter"), filterTab);
		tabs.addTab(ResourcesLoader.getString("settings.detector"), detectorTab);
		if (annotateTab != null) {
			tabs.addTab(ResourcesLoader.getString("settings.annotate"), annotateTab);
		}
		if (shareTab != null) {
			tabs.addTab(ResourcesLoader.getString("settings.share"), shareTab);
		}
		//add(tabs.getComponent()); // see comment above
		add(tabs);

		detectorTab.getTablePane().getTable().setBugCategory(reportTab.getBugCategory());
		if (generalTab != null) {
			generalTab.getPluginTablePane().setDetectorTablePane(detectorTab.getTablePane());
		}
	}

	@NotNull
	private ActionToolbar createToolbar() {
		final AdvancedSettingsAction actions = new AdvancedSettingsAction(this);
		final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);
		actionToolbar.setTargetComponent(this);
		return actionToolbar;
	}

	@Nullable
	abstract GeneralTab createGeneralTab();

	@Nullable
	abstract AnnotateTab createAnnotateTab();

	@Nullable
	abstract ShareTab createShareTab();

	@NotNull
	abstract AbstractSettings createSettings();

	final boolean isModified(@NotNull final AbstractSettings settings) {
		return reportTab.isModified(settings) ||
				filterTab.isModified(settings) ||
				detectorTab.isModified(settings);
	}

	final boolean isModifiedProject(@NotNull final ProjectSettings settings) {
		return generalTab.isModified(settings) ||
				annotateTab.isModifiedProject(settings);
	}

	final boolean isModifiedWorkspace(@NotNull final WorkspaceSettings settings) {
		return annotateTab.isModifiedWorkspace(settings) ||
				shareTab.isModified(settings);
	}

	final void apply(@NotNull final AbstractSettings settings) throws ConfigurationException {
		reportTab.apply(settings);
		filterTab.apply(settings);
		detectorTab.apply(settings);
	}

	final void applyProject(@NotNull final ProjectSettings settings) throws ConfigurationException {
		generalTab.apply(settings);
		annotateTab.applyProject(settings);
	}

	final void applyWorkspace(@NotNull final WorkspaceSettings settings) throws ConfigurationException {
		annotateTab.applyWorkspace(settings);
		shareTab.apply(settings);
	}

	final void reset(@NotNull final AbstractSettings settings) {
		reportTab.reset(settings);
		filterTab.reset(settings);
		detectorTab.reset(settings);
	}

	final void resetProject(@NotNull final ProjectSettings settings) {
		generalTab.reset(settings);
		annotateTab.resetProject(settings);
	}

	final void resetWorkspace(@NotNull final WorkspaceSettings settings) {
		annotateTab.resetWorkspace(settings);
		shareTab.reset(settings);
	}

	final void setFilter(String filter) {
		if (detectorTab != null) {
			detectorTab.setFilter(filter);
		}
	}

	@Override
	public void dispose() {
		if (detectorTab != null) {
			Disposer.dispose(detectorTab);
			detectorTab = null;
		}
		if (shareTab != null) {
			Disposer.dispose(shareTab);
			shareTab = null;
		}
	}
}
